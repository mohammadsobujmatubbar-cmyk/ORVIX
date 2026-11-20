package com.orvix.smarthome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.UUID

sealed class UdpResult {
    data class Acked(val relay: Int, val state: Int) : UdpResult()
    object Timeout : UdpResult()
    data class Error(val msg: String) : UdpResult()
}

data class FoundDevice(
    val ip: String,
    val name: String,
    val kind: String,
    val mac: String
)

object UdpManager {
    private const val RETRIES = 3
    private const val ACK_TIMEOUT_MS = 500

    // Command: একই id দিয়ে Retry হয়, ডিভাইস duplicate id উপেক্ষা করে শুধু আবার ACK দেবে
    suspend fun sendRelay(
        host: String,
        port: Int,
        relay: Int,
        state: Int,
        msg: String
    ): UdpResult = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString().take(8)
        val payload = JSONObject()
            .put("v", 1).put("type", "cmd").put("id", id)
            .put("relay", relay).put("state", state).put("msg", msg)
            .toString().toByteArray()
        try {
            val addr = InetAddress.getByName(host)
            DatagramSocket().use { s ->
                s.soTimeout = ACK_TIMEOUT_MS
                repeat(RETRIES) {
                    s.send(DatagramPacket(payload, payload.size, addr, port))
                    try {
                        val buf = ByteArray(512)
                        val p = DatagramPacket(buf, buf.size)
                        s.receive(p)
                        val j = JSONObject(String(p.data, 0, p.length))
                        if (j.optString("type") == "ack" && j.optString("id") == id) {
                            return@withContext UdpResult.Acked(
                                j.optInt("relay", relay), j.optInt("state", state)
                            )
                        }
                    } catch (_: SocketTimeoutException) {
                    }
                }
            }
            UdpResult.Timeout
        } catch (e: Exception) {
            UdpResult.Error(e.message ?: "UDP error")
        }
    }

    // Discovery: Broadcast করে ২ সেকেন্ড উত্তর শোনে
    suspend fun discover(port: Int = 4210, waitMs: Int = 2000): List<FoundDevice> =
        withContext(Dispatchers.IO) {
            val found = LinkedHashMap<String, FoundDevice>()
            try {
                DatagramSocket().use { s ->
                    s.broadcast = true
                    val req = JSONObject().put("v", 1).put("type", "discover")
                        .toString().toByteArray()
                    s.send(
                        DatagramPacket(
                            req, req.size,
                            InetAddress.getByName("255.255.255.255"), port
                        )
                    )
                    val end = System.currentTimeMillis() + waitMs
                    while (true) {
                        val left = (end - System.currentTimeMillis()).toInt()
                        if (left <= 0) break
                        s.soTimeout = left
                        try {
                            val buf = ByteArray(512)
                            val p = DatagramPacket(buf, buf.size)
                            s.receive(p)
                            val j = JSONObject(String(p.data, 0, p.length))
                            if (j.optString("type") == "hello") {
                                val ip = p.address.hostAddress ?: continue
                                found[ip] = FoundDevice(
                                    ip,
                                    j.optString("name", "Unknown"),
                                    j.optString("kind", ""),
                                    j.optString("mac", "")
                                )
                            }
                        } catch (_: SocketTimeoutException) {
                            break
                        } catch (_: Exception) {
                        }
                    }
                }
            } catch (_: Exception) {
            }
            found.values.toList()
        }
}
