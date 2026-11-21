package com.orvix.smarthome

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreenV3(
    s: Strings, rooms: List<RoomEntity>, devices: List<DeviceEntity>, relayCount: Int,
    onOpenRelay: (RelayEntity) -> Unit, modifier: Modifier = Modifier
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

    val sortedRooms = rooms.sortedByDescending { it.pinned }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val picked = selectedId
    val sel: Long = when {
        picked == -1L -> -1L
        picked != null && rooms.any { it.id == picked } -> picked
        else -> sortedRooms.firstOrNull { it.pinned }?.id ?: -1L
    }
    val shown = if (sel < 0) devices else devices.filter { it.roomId == sel }
    val cards = shown.flatMap { d ->
        allRelays.filter { it.deviceId == d.id }.sortedBy { it.id }.map { r -> Pair(d, r) }
    }

    Column(
        modifier
            .padding(bottom = 112.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item { GlassChip(s.all, sel < 0) { selectedId = -1L } }
            items(sortedRooms, key = { it.id }) { r -> GlassChip(r.name, sel == r.id) { selectedId = r.id } }
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
                    val code = cmd[r.id]
                    GlassCard(Modifier.weight(1f)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconBubble(r.icon, r.isOn)
                            ArrowBubble { onOpenRelay(r) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            rooms.firstOrNull { it.id == d.roomId }?.name ?: "",
                            color = g.subText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            r.name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            d.name + "  " + statusText(online[d.id]),
                            color = g.subText, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            if (code != null) relayCmdLabel(code) else " ",
                            color = g.subText, fontSize = 11.sp, maxLines = 1
                        )
                        Spacer(Modifier.height(8.dp))
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
        Spacer(Modifier.height(16.dp))
    }
}
