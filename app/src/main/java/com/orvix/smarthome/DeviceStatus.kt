package com.orvix.smarthome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

object DeviceStatus {
    // deviceId -> true (Online) / false (Offline); না থাকলে এখনো চেক হয়নি
    val map = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
}

suspend fun pingDevice(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
    try {
        val addr = InetAddress.getByName(host)
        val data = JSONObject().put("v", 1).put("type", "ping").toString().toByteArray()
        DatagramSocket().use { s ->
            s.soTimeout = 700
            repeat(2) {
                s.send(DatagramPacket(data, data.size, addr, port))
                try {
                    val buf = ByteArray(256)
                    val p = DatagramPacket(buf, buf.size)
                    s.receive(p)
                    if (JSONObject(String(p.data, 0, p.length)).optString("type") == "pong") {
                        return@withContext true
                    }
                } catch (_: SocketTimeoutException) {
                }
            }
        }
        false
    } catch (_: Exception) {
        false
    }
}

suspend fun pingAll(devices: List<DeviceEntity>) = coroutineScope {
    devices.map { d ->
        async {
            val ok = pingDevice(d.host, d.port)
            DeviceStatus.map.update { it + (d.id to ok) }
        }
    }.forEach { it.await() }
}
