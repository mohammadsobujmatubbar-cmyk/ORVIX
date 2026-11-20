package com.orvix.smarthome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val g = LocalGlass.current
    val dark = g.text.luminance() > 0.5f
    val shape = RoundedCornerShape(28.dp)
    val sheen = Color.White.copy(alpha = if (dark) 0.14f else 0.55f)
    val rim = Color.White.copy(alpha = if (dark) 0.55f else 0.95f)
    Column(
        modifier = modifier
            .clip(shape)
            .background(g.card)
            .background(Brush.verticalGradient(listOf(sheen, Color.Transparent)))
            .border(1.dp, Brush.verticalGradient(listOf(rim, g.cardBorder)), shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlowBackdrop() {
    val g = LocalGlass.current
    val dark = g.text.luminance() > 0.5f
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val a1 = if (dark) 0.22f else 0.30f
        val a2 = if (dark) 0.20f else 0.28f
        val a3 = if (dark) 0.16f else 0.24f
        val c1 = Offset(w * 0.12f, h * 0.16f)
        val c2 = Offset(w * 0.98f, h * 0.52f)
        val c3 = Offset(w * 0.10f, h * 0.88f)
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFFFFE600).copy(alpha = a1), Color.Transparent), center = c1, radius = w * 0.75f),
            radius = w * 0.75f, center = c1
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF4DA3FF).copy(alpha = a2), Color.Transparent), center = c2, radius = w * 0.7f),
            radius = w * 0.7f, center = c2
        )
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFF2EE6C5).copy(alpha = a3), Color.Transparent), center = c3, radius = w * 0.65f),
            radius = w * 0.65f, center = c3
        )
    }
}
