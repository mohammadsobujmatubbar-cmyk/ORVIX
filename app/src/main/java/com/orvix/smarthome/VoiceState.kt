package com.orvix.smarthome

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object VoiceState {
    var enabled by mutableStateOf(false)
    var durIdx by mutableStateOf(1)
    var lang by mutableStateOf("EN")
    var reply by mutableStateOf(true)
    var listening by mutableStateOf(false)
    var partial by mutableStateOf("")
    var note by mutableStateOf("")
    val log = mutableStateListOf<String>()
    var uiLang by mutableStateOf(Lang.EN)
    var onMicTap: (() -> Unit)? = null

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("orvix_settings", Context.MODE_PRIVATE)

    fun load(ctx: Context) {
        val p = prefs(ctx)
        enabled = p.getBoolean("voice_on", false)
        durIdx = p.getInt("voice_dur", 1)
        lang = p.getString("voice_lang", "EN") ?: "EN"
        reply = p.getBoolean("voice_reply", true)
    }

    fun setEnabled(ctx: Context, v: Boolean) {
        enabled = v
        prefs(ctx).edit().putBoolean("voice_on", v).apply()
    }

    fun setDur(ctx: Context, v: Int) {
        durIdx = v
        prefs(ctx).edit().putInt("voice_dur", v).apply()
    }

    fun setLang(ctx: Context, v: String) {
        lang = v
        prefs(ctx).edit().putString("voice_lang", v).apply()
    }

    fun setReply(ctx: Context, v: Boolean) {
        reply = v
        prefs(ctx).edit().putBoolean("voice_reply", v).apply()
    }

    fun durationMs(): Long = when (durIdx) {
        0 -> 30000L
        1 -> 60000L
        4 -> 5000L
        2 -> 300000L
        else -> 86400000L
    }

    fun addLog(line: String) {
        log.add(line)
        while (log.size > 8) log.removeAt(0)
    }

    fun replaceLog(old: String, new: String) {
        val i = log.indexOf(old)
        if (i >= 0) log[i] = new
    }

    fun clearAll() {
        log.clear()
        partial = ""
        note = ""
    }
}
