package com.orvix.smarthome

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MqttScreen(
    s: Strings,
    rooms: List<RoomEntity>,
    device: DeviceEntity?,
    conn: MqttConnEntity?,
    onCreate: (DeviceEntity, MqttConnEntity, String) -> Unit,
    onUpdate: (DeviceEntity, MqttConnEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    val ctx = LocalContext.current
    var name by remember(device?.id) { mutableStateOf(device?.name ?: "") }
    var roomId by remember(device?.id) { mutableStateOf(device?.roomId ?: rooms.firstOrNull()?.id ?: -1L) }
    var clientId by remember(device?.id) { mutableStateOf(conn?.clientId ?: "") }
    var broker by remember(device?.id) { mutableStateOf(conn?.broker ?: device?.host ?: "") }
    var portText by remember(device?.id) { mutableStateOf((conn?.port ?: device?.port ?: 1883).toString()) }
    var tls by remember(device?.id) { mutableStateOf(conn?.tls ?: false) }
    var user by remember(device?.id) { mutableStateOf(conn?.username ?: "") }
    var pass by remember(device?.id) {
        mutableStateOf(if (device != null) Secrets.get(ctx, "mqtt_pw_" + device.id) else "")
    }
    var showMore by remember { mutableStateOf(false) }
    val port = portText.toIntOrNull()
    val portOk = port != null && port in 1..65535
    val canSave = name.isNotBlank() && broker.isNotBlank() && portOk && rooms.any { it.id == roomId }

    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassChip("←", false) { onBack() }
            Text(s.mqttTitle, color = g.text, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("◌ " + s.mqttNotConnected, color = g.subText, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (rooms.isEmpty()) Text(s.noRooms, color = g.subText)
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(s.mqttConnName) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(s.roomLabel, color = g.subText, fontSize = 14.sp)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rooms.forEach { r -> GlassChip(r.name, roomId == r.id) { roomId = r.id } }
                }
                OutlinedTextField(
                    value = broker, onValueChange = { broker = it },
                    label = { Text(s.mqttBroker) },
                    placeholder = { Text("broker.example.com") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = portText,
                    onValueChange = { v -> portText = v.filter { c -> c.isDigit() }.take(5) },
                    label = { Text(s.portLabel) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !portOk,
                    supportingText = { if (!portOk) Text(s.invalidPort) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(s.mqttProtocol, color = g.subText, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip("TCP", !tls) {
                        tls = false
                        if (portText == "8883") portText = "1883"
                    }
                    GlassChip("TLS", tls) {
                        tls = true
                        if (portText == "1883") portText = "8883"
                    }
                }
                GlassChip(s.mqttMore, showMore) { showMore = !showMore }
                if (showMore) {
                    OutlinedTextField(
                        value = clientId, onValueChange = { clientId = it },
                        label = { Text(s.mqttClientId) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = user, onValueChange = { user = it },
                        label = { Text(s.mqttUser) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pass, onValueChange = { pass = it },
                        label = { Text(s.mqttPass) }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(s.mqttPassNote, color = g.subText, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassButton(s.save) {
            if (canSave) {
                val p = port ?: 1883
                val c = MqttConnEntity(
                    id = conn?.id ?: 0,
                    deviceId = device?.id ?: 0,
                    name = name.trim(),
                    clientId = clientId.trim(),
                    broker = broker.trim(),
                    port = p,
                    tls = tls,
                    username = user.trim()
                )
                if (device == null) {
                    onCreate(
                        DeviceEntity(
                            roomId = roomId, name = name.trim(), type = "MQTT",
                            host = broker.trim(), port = p
                        ),
                        c, pass
                    )
                } else {
                    Secrets.put(ctx, "mqtt_pw_" + device.id, pass)
                    onUpdate(
                        device.copy(roomId = roomId, name = name.trim(), host = broker.trim(), port = p),
                        c
                    )
                }
                onBack()
            }
        }
        Spacer(Modifier.height(120.dp))
    }
}
