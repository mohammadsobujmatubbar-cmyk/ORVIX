package com.orvix.smarthome

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object VoiceState {
    var enabled by mutableStateOf(false)
    var durIdx by mutableStateOf(1)
    var lang by mutableStateOf("EN")
    var listening by mutableStateOf(false)
    var heard by mutableStateOf("")
    var result by mutableStateOf("")
    var stamp by mutableStateOf(0L)
    var uiLang by mutableStateOf(Lang.EN)
    var onMicTap: (() -> Unit)? = null

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences("orvix_settings", Context.MODE_PRIVATE)

    fun load(ctx: Context) {
        val p = prefs(ctx)
        enabled = p.getBoolean("voice_on", false)
        durIdx = p.getInt("voice_dur", 1)
        lang = p.getString("voice_lang", "EN") ?: "EN"
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

    fun durationMs(): Long = when (durIdx) {
        0 -> 30000L
        1 -> 60000L
        2 -> 300000L
        else -> 86400000L
    }

    fun bubble(h: String, r: String) {
        heard = h
        result = r
        stamp = System.currentTimeMillis()
    }

    fun clearBubble() {
        heard = ""
        result = ""
    }
}
