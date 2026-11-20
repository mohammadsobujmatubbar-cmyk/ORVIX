package com.orvix.smarthome

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object CmdStatus {
    val map = MutableStateFlow<Map<Long, String>>(emptyMap())
    fun set(relayId: Long, s: String) = map.update { it + (relayId to s) }
}

object DiscoveryState {
    val found = MutableStateFlow<List<FoundDevice>>(emptyList())
    val busy = MutableStateFlow(false)
}

suspend fun runRelayCmd(
    device: DeviceEntity,
    relay: RelayEntity,
    on: Boolean,
    save: suspend (RelayEntity) -> Unit
) {
    val msg = if (on) relay.onMessage else relay.offMessage
    if (msg.isBlank()) {
        CmdStatus.set(relay.id, "NOMSG")
        return
    }
    CmdStatus.set(relay.id, "SENDING")
    when (sendRaw(device.host, device.port, msg)) {
        is RawResult.Reply -> {
            save(relay.copy(isOn = on))
            CmdStatus.set(relay.id, "OK")
        }
        is RawResult.Sent -> {
            save(relay.copy(isOn = on))
            CmdStatus.set(relay.id, "SENT")
        }
        is RawResult.Error -> CmdStatus.set(relay.id, "ERROR")
    }
}

fun OrvixViewModel.sendRelayCmd(device: DeviceEntity, relay: RelayEntity, on: Boolean) {
    viewModelScope.launch { runRelayCmd(device, relay, on) { updateRelay(it) } }
}

fun OrvixViewModel.sendAllCmd(device: DeviceEntity, on: Boolean) {
    relays.value.filter { it.deviceId == device.id }.sortedBy { it.id }
        .forEach { sendRelayCmd(device, it, on) }
}

fun OrvixViewModel.discoverDevices() {
    if (DiscoveryState.busy.value) return
    viewModelScope.launch {
        DiscoveryState.busy.value = true
        DiscoveryState.found.value = UdpManager.discover()
        DiscoveryState.busy.value = false
    }
}
