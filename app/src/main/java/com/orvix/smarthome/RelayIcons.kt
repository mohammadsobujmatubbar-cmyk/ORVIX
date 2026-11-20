package com.orvix.smarthome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

val RelayIconKeys = listOf("light", "fan", "tv", "ac", "plug", "other")

@Composable
fun RelayGlyph(key: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = (size.minDimension * 0.07f).coerceIn(3f, 12f)
        val st = Stroke(width = sw, cap = StrokeCap.Round)
        when (key) {
            "light" -> {
                drawCircle(color, radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.38f), style = st)
                drawLine(color, Offset(w * 0.4f, h * 0.76f), Offset(w * 0.6f, h * 0.76f), strokeWidth = sw, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.43f, h * 0.88f), Offset(w * 0.57f, h * 0.88f), strokeWidth = sw, cap = StrokeCap.Round)
            }
            "fan" -> {
                val c = Offset(w * 0.5f, h * 0.5f)
                listOf(0f, 120f, 240f).forEach { a ->
                    rotate(a, c) {
                        drawOval(color, topLeft = Offset(w * 0.4f, h * 0.08f), size = Size(w * 0.2f, h * 0.34f), style = st)
                    }
                }
                drawCircle(color, radius = w * 0.06f, center = c)
            }
            "tv" -> {
                drawRoundRect(color, topLeft = Offset(w * 0.1f, h * 0.2f), size = Size(w * 0.8f, h * 0.52f), cornerRadius = CornerRadius(w * 0.08f, w * 0.08f), style = st)
                drawLine(color, Offset(w * 0.35f, h * 0.84f), Offset(w * 0.65f, h * 0.84f), strokeWidth = sw, cap = StrokeCap.Round)
            }
            "ac" -> {
                drawRoundRect(color, topLeft = Offset(w * 0.08f, h * 0.22f), size = Size(w * 0.84f, h * 0.3f), cornerRadius = CornerRadius(w * 0.08f, w * 0.08f), style = st)
                drawLine(color, Offset(w * 0.28f, h * 0.65f), Offset(w * 0.28f, h * 0.82f), strokeWidth = sw, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.5f, h * 0.65f), Offset(w * 0.5f, h * 0.86f), strokeWidth = sw, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.72f, h * 0.65f), Offset(w * 0.72f, h * 0.82f), strokeWidth = sw, cap = StrokeCap.Round)
            }
            "plug" -> {
                drawLine(color, Offset(w * 0.38f, h * 0.1f), Offset(w * 0.38f, h * 0.3f), strokeWidth = sw, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.62f, h * 0.1f), Offset(w * 0.62f, h * 0.3f), strokeWidth = sw, cap = StrokeCap.Round)
                drawRoundRect(color, topLeft = Offset(w * 0.26f, h * 0.3f), size = Size(w * 0.48f, h * 0.32f), cornerRadius = CornerRadius(w * 0.1f, w * 0.1f), style = st)
                drawLine(color, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.5f, h * 0.9f), strokeWidth = sw, cap = StrokeCap.Round)
            }
            else -> {
                drawCircle(color, radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.5f), style = st)
                drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.5f))
            }
        }
    }
}

@Composable
fun IconBubble(key: String, on: Boolean) {
    val g = LocalGlass.current
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (on) Accent else g.chip)
            .border(1.dp, g.cardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        RelayGlyph(key, if (on) Color(0xFF111111) else g.text, Modifier.size(24.dp))
    }
}

@Composable
fun IconPickChip(key: String, selected: Boolean, onClick: () -> Unit) {
    val g = LocalGlass.current
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (selected) g.chipSelected else g.chip)
            .border(1.dp, g.cardBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        RelayGlyph(key, if (selected) g.chipSelectedText else g.text, Modifier.size(26.dp))
    }
}

@Composable
fun ArrowBubble(onClick: () -> Unit) {
    val g = LocalGlass.current
    val c = g.text
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(g.chip)
            .border(1.dp, g.cardBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
            val sw = 3f
            drawLine(c, Offset(w * 0.2f, h * 0.8f), Offset(w * 0.8f, h * 0.2f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(c, Offset(w * 0.3f, h * 0.2f), Offset(w * 0.8f, h * 0.2f), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(c, Offset(w * 0.8f, h * 0.2f), Offset(w * 0.8f, h * 0.7f), strokeWidth = sw, cap = StrokeCap.Round)
        }
    }
}
