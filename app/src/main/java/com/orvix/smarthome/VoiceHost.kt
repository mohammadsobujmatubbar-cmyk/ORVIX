package com.orvix.smarthome

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay

private class VoiceEngine(
    val ctx: Context,
    val vm: OrvixViewModel,
    val vsProvider: () -> VoiceStrings
) {
    private val handler = Handler(Looper.getMainLooper())
    private var rec: SpeechRecognizer? = null
    private var endAt = 0L
    private var errCount = 0
    private var running = false

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onResults(results: Bundle?) {
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            errCount = 0
            if (list != null) handleTexts(list)
            scheduleNext(300)
        }

        override fun onError(error: Int) {
            if (!running) return
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> scheduleNext(150)
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> stop(vsProvider().needPerm)
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> scheduleNext(700)
                else -> {
                    errCount++
                    if (errCount >= 4) {
                        stop(vsProvider().error + " (" + error + ")")
                    } else {
                        scheduleNext(900)
                    }
                }
            }
        }
    }

    fun start() {
        if (running) return
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) {
            VoiceState.bubble("", vsProvider().unavailable)
            return
        }
        running = true
        errCount = 0
        endAt = System.currentTimeMillis() + VoiceState.durationMs()
        VoiceState.listening = true
        VoiceState.clearBubble()
        listenOnce()
    }

    fun stop(msg: String? = null) {
        running = false
        handler.removeCallbacksAndMessages(null)
        try {
            rec?.cancel()
        } catch (_: Exception) {
        }
        try {
            rec?.destroy()
        } catch (_: Exception) {
        }
        rec = null
        VoiceState.listening = false
        if (msg != null) VoiceState.bubble("", msg)
    }

    private fun scheduleNext(ms: Long) {
        if (!running) return
        handler.postDelayed({ listenOnce() }, ms)
    }

    private fun listenOnce() {
        if (!running) return
        if (System.currentTimeMillis() >= endAt) {
            stop(vsProvider().timeUp)
            return
        }
        try {
            val r: SpeechRecognizer = rec ?: SpeechRecognizer.createSpeechRecognizer(ctx).also {
                it.setRecognitionListener(listener)
                rec = it
            }
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (VoiceState.lang == "BN") "bn-BD" else "en-US")
            i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.packageName)
            r.startListening(i)
        } catch (e: Exception) {
            stop(vsProvider().error)
        }
    }

    private fun handleTexts(list: List<String>) {
        if (list.isEmpty()) return
        val vs = vsProvider()
        val rooms = vm.rooms.value
        val devices = vm.devices.value
        val relays = vm.relays.value
        for (t in list) {
            val p = parseVoice(t, rooms, devices, relays)
            if (p is VoiceParse.Act) {
                for (pair in p.targets) {
                    vm.sendRelayCmd(pair.first, pair.second, p.on)
                }
                val label = if (p.all && p.targets.size > 3) {
                    vs.allDone + " (" + p.targets.size + ")"
                } else {
                    p.targets.joinToString(", ") { it.second.name }
                }
                VoiceState.bubble(t, label + " -> " + (if (p.on) vs.on else vs.off))
                return
            }
            if (p is VoiceParse.Ambiguous) {
                VoiceState.bubble(t, vs.ambiguous)
                return
            }
        }
        VoiceState.bubble(list[0], vs.noMatch)
    }
}

@Composable
fun VoiceHost(vm: OrvixViewModel, lang: Lang) {
    val ctx = LocalContext.current
    val engine = remember { VoiceEngine(ctx, vm) { voiceStrings(VoiceState.uiLang) } }
    SideEffect { VoiceState.uiLang = lang }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) {
            engine.start()
        } else {
            VoiceState.bubble("", voiceStrings(VoiceState.uiLang).needPerm)
        }
    }

    DisposableEffect(engine) {
        VoiceState.onMicTap = {
            val vs = voiceStrings(VoiceState.uiLang)
            if (!VoiceState.enabled) {
                VoiceState.bubble("", vs.enableFirst)
            } else if (VoiceState.listening) {
                engine.stop(vs.stopped)
            } else if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                engine.start()
            } else {
                permLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        onDispose {
            VoiceState.onMicTap = null
            engine.stop()
        }
    }

    val lifecycle = (ctx as? LifecycleOwner)?.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_STOP) engine.stop()
        }
        lifecycle?.addObserver(obs)
        onDispose { lifecycle?.removeObserver(obs) }
    }

    LaunchedEffect(VoiceState.enabled) {
        if (!VoiceState.enabled) engine.stop()
    }

    LaunchedEffect(VoiceState.stamp) {
        val st = VoiceState.stamp
        if (st != 0L) {
            delay(6000)
            if (VoiceState.stamp == st) VoiceState.clearBubble()
        }
    }
}
