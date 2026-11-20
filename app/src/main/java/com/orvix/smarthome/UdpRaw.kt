package com.orvix.smarthome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

sealed class RawResult {
    data class Reply(val text: String) : RawResult()
    object Sent : RawResult()
    data class Error(val msg: String) : RawResult()
}

suspend fun sendRaw(host: String, port: Int, message: String, waitMs: Int = 600): RawResult =
    withContext(Dispatchers.IO) {
        try {
            val addr = InetAddress.getByName(host)
            val data = message.toByteArray()
            DatagramSocket().use { s ->
                s.soTimeout = waitMs
                s.send(DatagramPacket(data, data.size, addr, port))
                try {
                    val buf = ByteArray(512)
                    val p = DatagramPacket(buf, buf.size)
                    s.receive(p)
                    RawResult.Reply(String(p.data, 0, p.length).trim())
                } catch (_: SocketTimeoutException) {
                    RawResult.Sent
                }
            }
        } catch (e: Exception) {
            RawResult.Error(e.message ?: "UDP error")
        }
    }
