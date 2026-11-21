package com.orvix.smarthome

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VoiceSettingsCard(lang: Lang) {
    val g = LocalGlass.current
    val ctx = LocalContext.current
    val vs = voiceStrings(lang)
    GlassCard(Modifier.fillMaxWidth()) {
        Text(vs.title, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassChip(vs.on, VoiceState.enabled) { VoiceState.setEnabled(ctx, true) }
            GlassChip(vs.off, !VoiceState.enabled) { VoiceState.setEnabled(ctx, false) }
        }
        Spacer(Modifier.height(14.dp))
        Text(vs.duration, color = g.subText, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassChip(if (lang == Lang.BN) "৫ সেকেন্ড" else "5 sec", VoiceState.durIdx == 4) { VoiceState.setDur(ctx, 4) }
            GlassChip(vs.d30, VoiceState.durIdx == 0) { VoiceState.setDur(ctx, 0) }
            GlassChip(vs.d1, VoiceState.durIdx == 1) { VoiceState.setDur(ctx, 1) }
            GlassChip(vs.d5, VoiceState.durIdx == 2) { VoiceState.setDur(ctx, 2) }
            GlassChip(vs.dOpen, VoiceState.durIdx == 3) { VoiceState.setDur(ctx, 3) }
        }
        Spacer(Modifier.height(14.dp))
        Text(vs.language, color = g.subText, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassChip("English", VoiceState.lang == "EN") { VoiceState.setLang(ctx, "EN") }
            GlassChip("বাংলা", VoiceState.lang == "BN") { VoiceState.setLang(ctx, "BN") }
        }
        Spacer(Modifier.height(14.dp))
        Text(vs.reply, color = g.subText, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GlassChip(vs.on, VoiceState.reply) { VoiceState.setReply(ctx, true) }
            GlassChip(vs.off, !VoiceState.reply) { VoiceState.setReply(ctx, false) }
        }
        Spacer(Modifier.height(12.dp))
        Text(vs.note, color = g.subText, fontSize = 12.sp)
    }
}
