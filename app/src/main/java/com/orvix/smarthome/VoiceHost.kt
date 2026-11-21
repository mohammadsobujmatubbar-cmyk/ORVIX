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
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import java.util.Locale

private fun hasBengali(t: String): Boolean = t.any { it in '\u0980'..'\u09FF' }

private class VoiceEngine(
    val ctx: Context,
    val vm: OrvixViewModel,
    val uiVs: () -> VoiceStrings
) {
    private val handler = Handler(Looper.getMainLooper())
    private var rec: SpeechRecognizer? = null
    private var endAt = 0L
    private var errCount = 0
    private var running = false

    private var tts: TextToSpeech? = null
    @Volatile private var ttsReady = false
    @Volatile private var speakEndAt = 0L

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            val l = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (l != null && l.isNotEmpty()) VoiceState.partial = l[0]
        }

        override fun onResults(results: Bundle?) {
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            errCount = 0
            VoiceState.partial = ""
            if (list != null) handleTexts(list)
            scheduleNext(300)
        }

        override fun onError(error: Int) {
            if (!running) return
            VoiceState.partial = ""
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> scheduleNext(150)
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> stop(uiVs().needPerm)
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> scheduleNext(700)
                else -> {
                    errCount++
                    if (errCount >= 4) {
                        stop(uiVs().error + " (" + error + ")")
                    } else {
                        scheduleNext(900)
                    }
                }
            }
        }
    }

    private fun ensureTts() {
        if (tts != null) return
        val t = TextToSpeech(ctx) { st -> ttsReady = (st == TextToSpeech.SUCCESS) }
        t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                speakEndAt = 0L
            }
            override fun onError(utteranceId: String?) {
                speakEndAt = 0L
            }
        })
        tts = t
    }

    private fun speak(text: String, bn: Boolean) {
        if (!VoiceState.reply) return
        ensureTts()
        val t = tts ?: return
        if (!ttsReady) return
        val loc = if (bn) Locale("bn", "BD") else Locale.US
        val r = t.setLanguage(loc)
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            VoiceState.note = uiVs().ttsMissing
            return
        }
        speakEndAt = System.currentTimeMillis() + 6000L
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "orvix")
    }

    fun start() {
        if (running) return
        if (!SpeechRecognizer.isRecognitionAvailable(ctx)) {
            VoiceState.note = uiVs().unavailable
            return
        }
        ensureTts()
        running = true
        errCount = 0
        endAt = System.currentTimeMillis() + VoiceState.durationMs()
        VoiceState.listening = true
        VoiceState.note = ""
        VoiceState.partial = ""
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
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
        speakEndAt = 0L
        VoiceState.listening = false
        VoiceState.partial = ""
        if (msg != null) VoiceState.note = msg
    }

    fun release() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        tts = null
        ttsReady = false
    }

    private fun scheduleNext(ms: Long) {
        if (!running) return
        handler.postDelayed({ listenOnce() }, ms)
    }

    private fun listenOnce() {
        if (!running) return
        if (System.currentTimeMillis() >= endAt) {
            stop(uiVs().timeUp)
            return
        }
        if (System.currentTimeMillis() < speakEndAt) {
            handler.postDelayed({ listenOnce() }, 300)
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
            i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.packageName)
            r.startListening(i)
        } catch (e: Exception) {
            stop(uiVs().error)
        }
    }

    private fun handleTexts(list: List<String>) {
        if (list.isEmpty()) return
        val ui = uiVs()
        val rooms = vm.rooms.value
        val devices = vm.devices.value
        val relays = vm.relays.value
        val heard = list[0]
        val bn = if (heard.isNotBlank()) hasBengali(heard) else VoiceState.lang == "BN"
        val sv = voiceStrings(if (bn) Lang.BN else Lang.EN)
        for (t in list) {
            val p = parseVoice(t, rooms, devices, relays)
            if (p is VoiceParse.Act) {
                for (pair in p.targets) {
                    vm.sendRelayCmd(pair.first, pair.second, p.on)
                }
                val label = if (p.all && p.targets.size > 3) {
                    ui.allDone + " (" + p.targets.size + ")"
                } else {
                    p.targets.joinToString(", ") { it.second.name }
                }
                val line = heard + "  ->  " + label + " " + (if (p.on) ui.on else ui.off)
                VoiceState.addLog(line)
                val firstId = p.targets[0].second.id
                handler.postDelayed({
                    val code = CmdStatus.map.value[firstId]
                    if (code != null) VoiceState.replaceLog(line, line + "  (" + relayCmdLabel(code) + ")")
                }, 1300)
                return
            }
            if (p is VoiceParse.Ambiguous) {
                VoiceState.addLog(heard + "  ->  " + ui.ambiguous)
                speak(sv.ambiguousSpoken, bn)
                return
            }
        }
        VoiceState.addLog(heard + "  ->  " + ui.noMatch)
        speak(sv.sorry, bn)
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
            VoiceState.note = voiceStrings(VoiceState.uiLang).needPerm
        }
    }

    DisposableEffect(engine) {
        VoiceState.onMicTap = {
            val vs = voiceStrings(VoiceState.uiLang)
            if (!VoiceState.enabled) {
                VoiceState.note = vs.enableFirst
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
            engine.release()
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
}
