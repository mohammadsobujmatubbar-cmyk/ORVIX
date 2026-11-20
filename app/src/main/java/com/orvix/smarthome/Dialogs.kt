package com.orvix.smarthome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun GlassButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(CircleShape)
            .background(Accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color(0xFF111111), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ConfirmDialog(
    title: String, message: String, confirmLabel: String, cancelLabel: String,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(cancelLabel) } }
    )
}

@Composable
fun RoomDialog(s: Strings, title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(s.roomName) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

@Composable
fun DeviceDialog(
    s: Strings, title: String, rooms: List<RoomEntity>, initial: DeviceEntity?,
    onDismiss: () -> Unit, onSave: (DeviceEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "ESP32") }
    var host by remember { mutableStateOf(initial?.host ?: "") }
    var portText by remember { mutableStateOf((initial?.port ?: 4210).toString()) }
    var checkMsg by remember { mutableStateOf(initial?.checkMessage ?: "") }
    var roomId by remember { mutableStateOf(initial?.roomId ?: rooms.firstOrNull()?.id ?: -1L) }
    val port = portText.toIntOrNull()
    val portOk = port != null && port in 1..65535
    val canSave = name.isNotBlank() && host.isNotBlank() && portOk && rooms.any { it.id == roomId }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (rooms.isEmpty()) Text(s.noRooms)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.deviceName) },
                    singleLine = true
                )
                Text(s.deviceType)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip("ESP32", type == "ESP32") { type = "ESP32" }
                    GlassChip("ESP8266", type == "ESP8266") { type = "ESP8266" }
                }
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(s.hostLabel) },
                    placeholder = { Text("192.168.0.100") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = portText,
                    onValueChange = { v -> portText = v.filter { c -> c.isDigit() }.take(5) },
                    label = { Text(s.portLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !portOk,
                    supportingText = { if (!portOk) Text(s.invalidPort) }
                )
                Text(s.roomLabel)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rooms.forEach { r -> GlassChip(r.name, roomId == r.id) { roomId = r.id } }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        DeviceEntity(
                            id = initial?.id ?: 0,
                            roomId = roomId,
                            name = name.trim(),
                            type = type,
                            host = host.trim(),
                            port = port ?: 4210,
                            checkMessage = checkMsg,
                            gatewayId = initial?.gatewayId
                        )
                    )
                },
                enabled = canSave
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}
