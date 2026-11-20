package com.orvix.smarthome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RelayDetailScreen(
    s: Strings, relay: RelayEntity, device: DeviceEntity, roomName: String,
    onBack: () -> Unit, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    val online = DeviceStatus.map.collectAsState().value
    val cmd = CmdStatus.map.collectAsState().value
    val code = cmd[relay.id]

    LaunchedEffect(device) {
        while (true) {
            checkDevice(device)
            delay(5000)
        }
    }

    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        GlassChip("<  " + s.back, false) { onBack() }
        Spacer(Modifier.height(24.dp))
        Text(relay.name, color = g.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(
            roomName + "  |  " + device.name + "  " + statusText(online[device.id]),
            color = g.subText, fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                if (relay.isOn) {
                    Canvas(Modifier.fillMaxSize()) {
                        val r = size.minDimension * 0.5f
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Accent.copy(alpha = 0.45f), Color.Transparent),
                                center = center, radius = r
                            ),
                            radius = r
                        )
                    }
                }
                RelayGlyph(relay.icon, if (relay.isOn) Accent else g.subText, Modifier.size(110.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                PowerPill(relay.isOn, if (relay.isOn) s.on else s.off) { onToggle(!relay.isOn) }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                if (code != null) relayCmdLabel(code) else " ",
                color = g.subText, fontSize = 12.sp, maxLines = 1,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(16.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text("ON: " + relay.onMessage.ifBlank { s.notSet }, color = g.subText, fontSize = 13.sp)
            Text("OFF: " + relay.offMessage.ifBlank { s.notSet }, color = g.subText, fontSize = 13.sp)
        }
        Spacer(Modifier.height(40.dp))
    }
}
