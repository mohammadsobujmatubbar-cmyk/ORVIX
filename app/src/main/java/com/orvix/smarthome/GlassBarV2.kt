package com.orvix.smarthome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MicGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = (size.minDimension * 0.08f).coerceIn(3f, 10f)
        val st = Stroke(width = sw, cap = StrokeCap.Round)
        drawRoundRect(
            color,
            topLeft = Offset(w * 0.36f, h * 0.06f),
            size = Size(w * 0.28f, h * 0.5f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
            style = st
        )
        drawArc(
            color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.28f),
            size = Size(w * 0.6f, h * 0.5f),
            style = st
        )
        drawLine(color, Offset(w * 0.5f, h * 0.78f), Offset(w * 0.5f, h * 0.92f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.35f, h * 0.92f), Offset(w * 0.65f, h * 0.92f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
private fun VoiceMicButton() {
    val g = LocalGlass.current
    val on = VoiceState.listening
    val enabled = VoiceState.enabled
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (on) Accent else Color.Transparent)
            .border(1.dp, if (on) Accent else g.cardBorder, CircleShape)
            .clickable { VoiceState.onMicTap?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        MicGlyph(
            if (on) Color(0xFF111111) else g.text.copy(alpha = if (enabled) 1f else 0.45f),
            Modifier.size(24.dp)
        )
    }
}

@Composable
private fun VoiceNavButton(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    val g = LocalGlass.current
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (selected) g.chipSelected else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = if (selected) g.chipSelectedText else g.text
        )
    }
}

@Composable
fun GlassBottomBarV2(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val g = LocalGlass.current
    val vs = voiceStrings(VoiceState.uiLang)
    val mid = items.size / 2
    val heard = VoiceState.heard
    val res = VoiceState.result
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (VoiceState.listening || heard.isNotBlank() || res.isNotBlank()) {
            Column(
                Modifier
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xDD101216))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (heard.isNotBlank()) {
                    Text(vs.heardLabel + ": " + heard, color = Color(0xB3FFFFFF), fontSize = 12.sp)
                }
                if (res.isNotBlank()) {
                    Text(res, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                if (heard.isBlank() && res.isBlank()) {
                    Text(vs.listening, color = Color.White, fontSize = 15.sp)
                }
            }
        }
        Row(
            Modifier
                .clip(CircleShape)
                .background(g.card)
                .border(1.dp, g.cardBorder, CircleShape)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEachIndexed { i, item ->
                if (i == mid) VoiceMicButton()
                VoiceNavButton(item, i == selected) { onSelect(i) }
            }
        }
    }
}
