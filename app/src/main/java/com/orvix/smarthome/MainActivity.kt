package com.orvix.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val vm: OrvixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MqttManager.init(applicationContext)
        val prefs = getSharedPreferences("orvix_settings", MODE_PRIVATE)
        BgState.load(this)
        val savedTheme = try {
            ThemeMode.valueOf(prefs.getString("theme", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
        val savedLang = try {
            Lang.valueOf(prefs.getString("lang", Lang.EN.name) ?: Lang.EN.name)
        } catch (e: Exception) {
            Lang.EN
        }
        setContent {
            var mode by remember { mutableStateOf(savedTheme) }
            var lang by remember { mutableStateOf(savedLang) }
            OrvixTheme(mode) {
                OrvixApp(
                    lang = lang,
                    mode = mode,
                    onTheme = { mode = it; prefs.edit().putString("theme", it.name).apply() },
                    onLang = { lang = it; prefs.edit().putString("lang", it.name).apply() },
                    vm = vm
                )
            }
        }
    }
}
