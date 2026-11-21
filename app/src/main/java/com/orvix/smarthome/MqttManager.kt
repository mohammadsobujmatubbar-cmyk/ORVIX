package com.orvix.smarthome

import android.content.Context
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

object MqttManager {
    // deviceId -> "Connecting..." / "" (সংযুক্ত) / ব্যর্থতার কারণ
    val detail = MutableStateFlow<Map<Long, String>>(emptyMap())

    private class Entry(val key: String, val client: Mqtt3AsyncClient) {
        @Volatile var failed = false
    }

    private val lock = Any()
    private val clients = HashMap<Long, Entry>()
    private var appCtx: Context? = null
    private var started = false

    @Volatile private var latestDevices: List<DeviceEntity> = emptyList()
    @Volatile private var latestConns: List<MqttConnEntity> = emptyList()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(context: Context) {
        if (started) return
        started = true
        val ctx = context.applicationContext
        appCtx = ctx
        val dao = OrvixDatabase.get(ctx).dao()
        scope.launch {
            combine(dao.observeDevices(), dao.observeMqtt()) { d, c -> Pair(d, c) }
                .collectLatest { p ->
                    latestDevices = p.first
                    latestConns = p.second
                    delay(300)
                    reconcile()
                }
        }
        scope.launch {
            while (true) {
                delay(15000)
                reconcile()
            }
        }
    }

    private fun reconcile() {
        val ctx = appCtx ?: return
        synchronized(lock) {
            val wanted = latestDevices.filter { it.type.equals("MQTT", true) }
            val ids = wanted.map { it.id }.toSet()
            for (id in clients.keys.toList()) {
                if (id !in ids) drop(id, false)
            }
            for (d in wanted) {
                val c = latestConns.firstOrNull { it.deviceId == d.id } ?: continue
                val pw = Secrets.get(ctx, "mqtt_pw_" + d.id)
                val key = listOf(c.broker, c.port, c.tls, c.username, c.clientId, pw).joinToString("|")
                val cur = clients[d.id]
                if (cur != null && cur.key == key && !cur.failed) continue
                if (cur != null) drop(d.id, true)
                connect(d.id, c, pw, key)
            }
        }
    }

    private fun drop(id: Long, keepStatus: Boolean) {
        val e = clients.remove(id)
        try {
            e?.client?.disconnect()
        } catch (_: Exception) {
        }
        if (!keepStatus) {
            DeviceStatus.map.update { it - id }
            detail.update { it - id }
        }
    }

    private fun connect(id: Long, c: MqttConnEntity, pw: String, key: String) {
        val host = c.broker.trim().substringAfter("://").trimEnd('/')
        if (host.isBlank()) {
            setStatus(id, false, "no broker address")
            return
        }
        setStatus(id, null, "Connecting...")
        try {
            val cid = c.clientId.trim().ifBlank {
                "orvix-" + id + "-" + UUID.randomUUID().toString().take(6)
            }
            var b = MqttClient.builder().useMqttVersion3()
                .identifier(cid).serverHost(host).serverPort(c.port)
            if (c.tls) b = b.sslWithDefaultConfig()
            val client = b
                .automaticReconnectWithDefaultConfig()
                .addConnectedListener { setStatus(id, true, "") }
                .addDisconnectedListener { dc -> setStatus(id, false, reasonOf(dc.cause)) }
                .buildAsync()
            val entry = Entry(key, client)
            clients[id] = entry
            val fut = if (c.username.isNotBlank()) {
                client.connectWith().keepAlive(30)
                    .simpleAuth().username(c.username).password(pw.toByteArray()).applySimpleAuth()
                    .send()
            } else {
                client.connectWith().keepAlive(30).send()
            }
            fut.whenComplete { _, err ->
                if (err != null) {
                    entry.failed = true
                    setStatus(id, false, reasonOf(err))
                }
            }
        } catch (e: Exception) {
            setStatus(id, false, reasonOf(e))
        }
    }

    // পরের ধাপে Relay Switch এটা ব্যবহার করবে
    fun publish(deviceId: Long, topic: String, payload: String): Boolean {
        val e = synchronized(lock) { clients[deviceId] } ?: return false
        return try {
            if (!e.client.state.isConnected) return false
            e.client.publishWith().topic(topic).payload(payload.toByteArray())
                .qos(MqttQos.AT_LEAST_ONCE).send()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun setStatus(id: Long, online: Boolean?, text: String) {
        DeviceStatus.map.update { m -> if (online == null) m - id else m + (id to online) }
        detail.update { it + (id to text) }
    }

    private fun reasonOf(t: Throwable?): String {
        var e = t
        var text = ""
        var depth = 0
        while (e != null && depth < 6) {
            text += " " + e.javaClass.simpleName + " " + (e.message ?: "")
            e = e.cause
            depth++
        }
        return when {
            text.contains("BAD_USER_NAME_OR_PASSWORD") || text.contains("NOT_AUTHORIZED") -> "wrong username/password"
            text.contains("UnknownHost", true) || text.contains("resolve", true) -> "broker address not found"
            text.contains("timed out", true) || text.contains("timeout", true) -> "timed out"
            text.contains("refused", true) -> "connection refused"
            text.contains("SSL", true) || text.contains("certificate", true) || text.contains("handshake", true) -> "TLS/certificate problem"
            else -> text.trim().take(60)
        }
    }
}
