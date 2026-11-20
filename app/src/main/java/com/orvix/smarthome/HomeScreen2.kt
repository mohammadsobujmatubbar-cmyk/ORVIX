package com.orvix.smarthome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun relayCmdLabel(code: String): String = when (code) {
    "SENDING" -> "Sending..."
    "OK" -> "Reply received"
    "SENT" -> "Sent"
    "NOMSG" -> "Message not set"
    else -> "Error"
}

@Composable
fun HomeScreen(
    s: Strings, rooms: List<RoomEntity>, devices: List<DeviceEntity>, relayCount: Int,
    modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    val ctx = LocalContext.current
    val dao = remember { OrvixDatabase.get(ctx).dao() }
    val relayFlow = remember { dao.observeRelays() }
    val allRelays = relayFlow.collectAsState(initial = emptyList()).value
    val scope = rememberCoroutineScope()
    val cmd = CmdStatus.map.collectAsState().value
    val online = DeviceStatus.map.collectAsState().value

    LaunchedEffect(devices) {
        while (true) {
            pingAll(devices)
            delay(5000)
        }
    }

    var selectedId by remember { mutableStateOf(-1L) }
    val sel = if (rooms.any { it.id == selectedId }) selectedId else -1L
    val shown = if (sel < 0) devices else devices.filter { it.roomId == sel }
    val cards = shown.flatMap { d ->
        allRelays.filter { it.deviceId == d.id }.sortedBy { it.id }.map { r -> Pair(d, r) }
    }
    val checked = devices.filter { it.checkMessage.isNotBlank() }

    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End)) {
            RoundIcon(Icons.Default.Notifications, "Notifications")
            RoundIcon(Icons.Default.Add, "Add")
        }
        Spacer(Modifier.height(20.dp))
        Text(s.welcome, color = g.text, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
        Spacer(Modifier.height(20.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item { GlassChip(s.all, sel < 0) { selectedId = -1L } }
            items(rooms, key = { it.id }) { r -> GlassChip(r.name, sel == r.id) { selectedId = r.id } }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(s.totalRooms, rooms.size.toString(), Modifier.weight(1f))
            StatCard(s.controllers, devices.size.toString(), Modifier.weight(1f))
            StatCard(s.relays, relayCount.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))

        cards.chunked(2).forEach { pair ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { item ->
                    val d = item.first
                    val r = item.second
                    GlassCard(Modifier.weight(1f)) {
                        Text(rooms.firstOrNull { it.id == d.roomId }?.name ?: "", color = g.subText, fontSize = 12.sp)
                        Text(r.name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text(d.name + "  " + statusText(online[d.id]), color = g.subText, fontSize = 11.sp)
                        val code = cmd[r.id]
                        if (code != null) Text(relayCmdLabel(code), color = g.subText, fontSize = 11.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            PowerPill(r.isOn, if (r.isOn) s.on else s.off) {
                                scope.launch { runRelayCmd(d, r, !r.isOn) { dao.updateRelay(it) } }
                            }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (cards.isEmpty()) {
            Text(
                if (devices.isEmpty()) s.noControllers else "No relays yet. Devices > Open > add relays",
                color = g.subText, fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Text(s.systemStatus, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(s.appReady, color = g.subText)
            if (checked.isEmpty()) {
                Text(s.notConfigured, color = g.subText)
            } else {
                Text("Online: " + checked.count { online[it.id] == true } + " / " + checked.size, color = g.subText)
            }
        }
        Spacer(Modifier.height(120.dp))
    }
}
