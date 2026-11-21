package com.orvix.smarthome

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BackupCard(lang: Lang, mode: ThemeMode, onLang: (Lang) -> Unit, onTheme: (ThemeMode) -> Unit) {
    val g = LocalGlass.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val bn = lang == Lang.BN
    var note by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<Uri?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                note = try {
                    val st = BackupSettings(
                        mode.name, lang.name, VoiceState.enabled, VoiceState.durIdx,
                        VoiceState.lang, VoiceState.reply
                    )
                    val text = BackupManager.buildJson(ctx, st)
                    withContext(Dispatchers.IO) {
                        ctx.contentResolver.openOutputStream(uri, "wt")!!.use {
                            it.write(text.toByteArray(Charsets.UTF_8))
                        }
                    }
                    if (bn) "ব্যাকআপ সেভ হয়েছে ✓" else "Backup saved ✓"
                } catch (e: Exception) {
                    (if (bn) "ব্যাকআপ হয়নি: " else "Backup failed: ") + (e.message ?: "")
                }
            }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pending = uri }

    GlassCard(Modifier.fillMaxWidth()) {
        Text(
            if (bn) "ব্যাকআপ" else "Backup",
            color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassChip(if (bn) "Backup" else "Backup", false) { saveLauncher.launch("orvix.json") }
            GlassChip(if (bn) "Restore" else "Restore", false) { openLauncher.launch(arrayOf("*/*")) }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (bn) "রুম, ডিভাইস, রিলে, সেন্সর, MQTT ও সেটিং orvix.json ফাইলে সেভ হবে। MQTT পাসওয়ার্ড ও ব্যাকগ্রাউন্ড ছবি ফাইলে যায় না।"
            else "Rooms, devices, relays, sensors, MQTT and settings are saved to orvix.json. MQTT passwords and the background photo are not included.",
            color = g.subText, fontSize = 12.sp
        )
        if (note.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(note, color = g.text, fontSize = 13.sp)
        }
    }

    pending?.let { uri ->
        ConfirmDialog(
            if (bn) "Restore" else "Restore",
            if (bn) "বর্তমান সব রুম, ডিভাইস ও রিলে মুছে ফাইলের ডাটা বসানো হবে। চালিয়ে যাবেন?"
            else "All current rooms, devices and relays will be replaced by the file's data. Continue?",
            if (bn) "Restore" else "Restore",
            if (bn) "বাতিল" else "Cancel",
            {
                pending = null
                scope.launch {
                    note = try {
                        val text = withContext(Dispatchers.IO) {
                            ctx.contentResolver.openInputStream(uri)!!.use {
                                it.readBytes().toString(Charsets.UTF_8)
                            }
                        }
                        val st = BackupManager.restore(ctx, text)
                        if (st != null) {
                            runCatching { onTheme(ThemeMode.valueOf(st.theme)) }
                            runCatching { onLang(Lang.valueOf(st.lang)) }
                            VoiceState.setEnabled(ctx, st.voiceOn)
                            VoiceState.setDur(ctx, st.voiceDur)
                            VoiceState.setLang(ctx, st.voiceLang)
                            VoiceState.setReply(ctx, st.voiceReply)
                        }
                        if (bn) "রিস্টোর হয়েছে ✓ (MQTT পাসওয়ার্ড আবার দিন)" else "Restored ✓ (re-enter MQTT passwords)"
                    } catch (e: Exception) {
                        (if (bn) "রিস্টোর হয়নি: " else "Restore failed: ") + (e.message ?: "")
                    }
                }
            },
            { pending = null }
        )
    }
}
