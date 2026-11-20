package com.orvix.smarthome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private fun cmdLabel(code: String): String = when (code) {
    "SENDING" -> "Sending..."
    "OK" -> "Reply received"
    "SENT" -> "Sent"
    "NOMSG" -> "Message not set"
    else -> "Error"
}

@Composable
fun HomeRelays(s: Strings, d: DeviceEntity) {
    val g = LocalGlass.current
    val ctx = LocalContext.current
    val dao = remember { OrvixDatabase.get(ctx).dao() }
    val flow = remember { dao.observeRelays() }
    val all = flow.collectAsState(initial = emptyList()).value
    val mine = all.filter { it.deviceId == d.id }.sortedBy { it.id }
    val scope = rememberCoroutineScope()
    val st = CmdStatus.map.collectAsState().value

    mine.forEach { r ->
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(r.name, color = g.text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val code = st[r.id]
                if (code != null) Text(cmdLabel(code), color = g.subText, fontSize = 11.sp)
            }
            PowerPill(r.isOn, if (r.isOn) s.on else s.off) {
                scope.launch { runRelayCmd(d, r, !r.isOn) { dao.updateRelay(it) } }
            }
        }
    }
}
