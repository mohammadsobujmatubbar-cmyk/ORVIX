package com.orvix.smarthome

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RelayDialog(
    s: Strings, title: String, deviceId: Long, initial: RelayEntity?,
    onDismiss: () -> Unit, mqtt: Boolean = false, onSave: (List<RelayEntity>) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var onMsg by remember { mutableStateOf(initial?.onMessage ?: "") }
    var offMsg by remember { mutableStateOf(initial?.offMessage ?: "") }
    var topic by remember { mutableStateOf(initial?.topic ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: "light") }
    var countText by remember { mutableStateOf("1") }
    val count = countText.toIntOrNull() ?: 0
    val adding = initial == null
    val countOk = !adding || count in 1..32
    val multi = adding && count > 1
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.relayName) },
                    singleLine = true
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RelayIconKeys.forEach { k -> IconPickChip(k, icon == k) { icon = k } }
                }
                if (adding) {
                    OutlinedTextField(
                        value = countText,
                        onValueChange = { v -> countText = v.filter { c -> c.isDigit() }.take(2) },
                        label = { Text(s.countLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = !countOk
                    )
                }
                if (mqtt && !multi) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic") },
                        placeholder = { Text("orvix/room/light1") },
                        singleLine = true
                    )
                }
                if (multi) {
                    Text(s.countHint)
                } else {
                    OutlinedTextField(
                        value = onMsg,
                        onValueChange = { onMsg = it },
                        label = { Text(if (mqtt) "ON payload" else s.onMessage) },
                        placeholder = { Text("LIGHT_ON") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = offMsg,
                        onValueChange = { offMsg = it },
                        label = { Text(if (mqtt) "OFF payload" else s.offMessage) },
                        placeholder = { Text("LIGHT_OFF") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val list = if (initial != null) {
                        listOf(initial.copy(name = name.trim(), onMessage = onMsg.trim(), offMessage = offMsg.trim(), icon = icon, topic = topic.trim()))
                    } else if (count > 1) {
                        (1..count).map { i ->
                            RelayEntity(deviceId = deviceId, name = name.trim() + " " + i, onMessage = "", offMessage = "", icon = icon)
                        }
                    } else {
                        listOf(RelayEntity(deviceId = deviceId, name = name.trim(), onMessage = onMsg.trim(), offMessage = offMsg.trim(), icon = icon, topic = topic.trim()))
                    }
                    onSave(list)
                },
                enabled = name.isNotBlank() && countOk
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

@Composable
fun SensorDialog(
    s: Strings, title: String, deviceId: Long, initial: SensorEntity?,
    onDismiss: () -> Unit, mqtt: Boolean = false, onSave: (SensorEntity) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "Temperature") }
    var config by remember { mutableStateOf(initial?.config ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s.sensorName) },
                    singleLine = true
                )
                Text(s.sensorType)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorTypes.forEach { t ->
                        GlassChip(sensorTypeLabel(s, t), type == t) { type = t }
                    }
                }
                OutlinedTextField(
                    value = config,
                    onValueChange = { config = it },
                    label = { Text(if (mqtt) "Topic" else s.sensorConfig) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        SensorEntity(
                            id = initial?.id ?: 0,
                            deviceId = deviceId,
                            name = name.trim(),
                            type = type,
                            config = config.trim()
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}
