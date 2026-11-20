package com.orvix.smarthome

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// প্রতিটি Relay-এর শেষ Command-এর অবস্থা: SENDING / OK / TIMEOUT / ERROR
object CmdStatus {
    val map = MutableStateFlow<Map<Long, String>>(emptyMap())
    fun set(relayId: Long, s: String) = map.update { it + (relayId to s) }
}

object DiscoveryState {
    val found = MutableStateFlow<List<FoundDevice>>(emptyList())
    val busy = MutableStateFlow(false)
}

// Relay নম্বর = ওই ডিভাইসের Relay তালিকায় অবস্থান (id ক্রমে, ১ থেকে)
fun OrvixViewModel.sendRelayCmd(device: DeviceEntity, relay: RelayEntity, on: Boolean) {
    val list = relays.value.filter { it.deviceId == device.id }.sortedBy { it.id }
    val index = list.indexOfFirst { it.id == relay.id } + 1
    if (index <= 0) return
    viewModelScope.launch {
        CmdStatus.set(relay.id, "SENDING")
        val msg = if (on) relay.onMessage else relay.offMessage
        when (val r = UdpManager.sendRelay(device.host, device.port, index, if (on) 1 else 0, msg)) {
            is UdpResult.Acked -> {
                // ACK আসার পরই অ্যাপে অবস্থা বদলায়
                val cur = relays.value.firstOrNull { it.id == relay.id } ?: relay
                updateRelay(cur.copy(isOn = r.state == 1))
                CmdStatus.set(relay.id, "OK")
            }
            is UdpResult.Timeout -> CmdStatus.set(relay.id, "TIMEOUT")
            is UdpResult.Error -> CmdStatus.set(relay.id, "ERROR")
        }
    }
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
