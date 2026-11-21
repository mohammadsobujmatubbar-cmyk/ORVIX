package com.orvix.smarthome

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object DeviceStatus {
    val map = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
}

fun statusText(v: Boolean?): String = when (v) {
    true -> "● Online"
    false -> "○ Offline"
    null -> "◌ Unknown"
}

suspend fun checkDevice(d: DeviceEntity) {
    if (d.type.equals("MQTT", true)) return
    if (d.checkMessage.isBlank()) {
        DeviceStatus.map.update { it - d.id }
        return
    }
    val r = sendRaw(d.host, d.port, d.checkMessage, 800)
    DeviceStatus.map.update { it + (d.id to (r is RawResult.Reply)) }
}

suspend fun pingAll(devices: List<DeviceEntity>) = coroutineScope {
    devices.map { d -> async { checkDevice(d) } }.forEach { it.await() }
}
