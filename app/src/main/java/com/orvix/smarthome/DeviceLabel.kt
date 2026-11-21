package com.orvix.smarthome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun deviceStatusLabel(d: DeviceEntity): String {
    val m = DeviceStatus.map.collectAsState().value[d.id]
    if (!d.type.equals("MQTT", true)) return statusText(m)
    val detail = MqttManager.detail.collectAsState().value[d.id] ?: ""
    if (detail == "Connecting...") return "◌ Connecting..."
    if (m == false && detail.isNotBlank()) return statusText(m) + ": " + detail
    return statusText(m)
}
