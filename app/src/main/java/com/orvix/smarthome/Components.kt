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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(val icon: ImageVector, val label: String)

@Composable
fun GlassCardOld(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val g = LocalGlass.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(g.card)
            .border(1.dp, g.cardBorder, RoundedCornerShape(28.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlassChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val g = LocalGlass.current
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (selected) g.chipSelected else g.chip)
            .border(1.dp, g.cardBorder, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(text, color = if (selected) g.chipSelectedText else g.text)
    }
}

@Composable
fun RoundIcon(icon: ImageVector, desc: String) {
    val g = LocalGlass.current
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(g.card)
            .border(1.dp, g.cardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = g.text)
    }
}

@Composable
fun PowerIcon(color: Color) {
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val sw = 2.5.dp.toPx()
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(w * 0.1f, h * 0.18f),
            size = Size(w * 0.8f, h * 0.8f),
            style = Stroke(width = sw, cap = StrokeCap.Round)
        )
        drawLine(color, Offset(w / 2f, sw / 2f), Offset(w / 2f, h * 0.5f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}

@Composable
fun PowerDot(on: Boolean) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (on) Accent else Color.White),
        contentAlignment = Alignment.Center
    ) {
        PowerIcon(Color(0xFF111111))
    }
}

@Composable
fun PowerPill(isOn: Boolean, label: String, onToggle: () -> Unit) {
    val g = LocalGlass.current
    Row(
        Modifier
            .clip(CircleShape)
            .background(g.chip)
            .clickable(onClick = onToggle)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isOn) {
            Text(label, color = g.subText, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
            PowerDot(true)
        } else {
            PowerDot(false)
            Text(label, color = g.subText, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 12.dp))
        }
    }
}

@Composable
fun DeviceCard(
    room: String, name: String, isOn: Boolean, onLabel: String, offLabel: String,
    onToggle: () -> Unit, modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    GlassCard(modifier) {
        Text(room, color = g.subText, fontSize = 12.sp)
        Text(name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            PowerPill(isOn, if (isOn) onLabel else offLabel, onToggle)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val g = LocalGlass.current
    GlassCard(modifier) {
        Text(value, color = g.text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label, color = g.subText, fontSize = 12.sp)
    }
}

@Composable
fun GlassBottomBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val g = LocalGlass.current
    Row(
        modifier
            .clip(CircleShape)
            .background(g.card)
            .border(1.dp, g.cardBorder, CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEachIndexed { i, item ->
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (i == selected) g.chipSelected else Color.Transparent)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = item.label,
                    tint = if (i == selected) g.chipSelectedText else g.text
                )
            }
        }
    }
}
