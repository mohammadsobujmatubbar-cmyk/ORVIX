package com.orvix.smarthome

import kotlinx.coroutines.flow.update

import androidx.compose.runtime.collectAsState

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DevicePanelScreen(
    s: Strings, device: DeviceEntity, roomName: String,
    relays: List<RelayEntity>, sensors: List<SensorEntity>,
    onBack: () -> Unit,
    onAddRelays: (List<RelayEntity>) -> Unit,
    onUpdateRelay: (RelayEntity) -> Unit,
    onDeleteRelay: (RelayEntity) -> Unit,
    onSetAll: (Boolean) -> Unit,
    onToggle: (RelayEntity, Boolean) -> Unit,
    onAddSensor: (SensorEntity) -> Unit,
    onUpdateSensor: (SensorEntity) -> Unit,
    onDeleteSensor: (SensorEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    var showAddRelay by remember { mutableStateOf(false) }
    var editingRelay by remember { mutableStateOf<RelayEntity?>(null) }
    var deletingRelay by remember { mutableStateOf<RelayEntity?>(null) }
    var showAddSensor by remember { mutableStateOf(false) }
    var editingSensor by remember { mutableStateOf<SensorEntity?>(null) }
    var deletingSensor by remember { mutableStateOf<SensorEntity?>(null) }

    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        GlassChip("<  " + s.back, false) { onBack() }
        Spacer(Modifier.height(16.dp))
        Text(device.name, color = g.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(statusText(DeviceStatus.map.collectAsState().value[device.id]), color = g.subText, fontSize = 14.sp)
        Text(
            roomName + "  |  " + device.type + "  |  " + device.host + ":" + device.port,
            color = g.subText, fontSize = 13.sp
        )
        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(s.relays, color = g.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            GlassButton("+ " + s.addRelay) { showAddRelay = true }
        }
        Spacer(Modifier.height(10.dp))
        if (relays.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(s.allOn, false) { relays.forEach { onToggle(it, true) } }
                GlassChip(s.allOff, false) { relays.forEach { onToggle(it, false) } }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (relays.isEmpty()) {
            GlassCard(Modifier.fillMaxWidth()) { Text(s.noRelays, color = g.subText) }
        }
        relays.forEach { r ->
            GlassCard(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(r.name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("ON: " + r.onMessage.ifBlank { s.notSet }, color = g.subText, fontSize = 12.sp)
                        Text("OFF: " + r.offMessage.ifBlank { s.notSet }, color = g.subText, fontSize = 12.sp)
                    }
                    PowerPill(r.isOn, if (r.isOn) s.on else s.off) { onToggle(r, !r.isOn) }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(s.edit, false) { editingRelay = r }
                    GlassChip(s.delete, false) { deletingRelay = r }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(s.sensorsTitle, color = g.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            GlassButton("+ " + s.addSensor) { showAddSensor = true }
        }
        Spacer(Modifier.height(10.dp))
        if (sensors.isEmpty()) {
            GlassCard(Modifier.fillMaxWidth()) { Text(s.noSensors, color = g.subText) }
        }
        sensors.forEach { sn ->
            GlassCard(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(sn.name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(sensorTypeLabel(s, sn.type), color = g.subText, fontSize = 13.sp)
                Text(sn.config.ifBlank { s.notSet }, color = g.subText, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(s.edit, false) { editingSensor = sn }
                    GlassChip(s.delete, false) { deletingSensor = sn }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(s.controlNote, color = g.subText, fontSize = 12.sp)
        Spacer(Modifier.height(40.dp))
    }

    if (showAddRelay) {
        RelayDialog(s, s.addRelay, device.id, null, { showAddRelay = false }) {
            onAddRelays(it); showAddRelay = false
        }
    }
    editingRelay?.let { r ->
        RelayDialog(s, s.editRelay, device.id, r, { editingRelay = null }) {
            onUpdateRelay(it.first()); editingRelay = null
        }
    }
    deletingRelay?.let { r ->
        ConfirmDialog(s.delete, s.deleteRelayMsg, s.delete, s.cancel, { onDeleteRelay(r); deletingRelay = null }, { deletingRelay = null })
    }
    if (showAddSensor) {
        SensorDialog(s, s.addSensor, device.id, null, { showAddSensor = false }) {
            onAddSensor(it); showAddSensor = false
        }
    }
    editingSensor?.let { sn ->
        SensorDialog(s, s.editSensor, device.id, sn, { editingSensor = null }) {
            onUpdateSensor(it); editingSensor = null
        }
    }
    deletingSensor?.let { sn ->
        ConfirmDialog(s.delete, s.deleteSensorMsg, s.delete, s.cancel, { onDeleteSensor(sn); deletingSensor = null }, { deletingSensor = null })
    }
}
