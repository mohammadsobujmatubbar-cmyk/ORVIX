package com.orvix.smarthome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun DarkGlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val g = LocalGlass.current
    val dark = g.text.luminance() > 0.5f
    val shape = RoundedCornerShape(28.dp)
    val base = if (dark) Color(0xCC101216) else Color(0xE6FFFFFF)
    val sheen = Color.White.copy(alpha = if (dark) 0.08f else 0.35f)
    Column(
        modifier = modifier
            .clip(shape)
            .background(base)
            .background(Brush.verticalGradient(listOf(sheen, Color.Transparent)))
            .border(1.dp, g.cardBorder, shape)
            .padding(16.dp),
        content = content
    )
}
