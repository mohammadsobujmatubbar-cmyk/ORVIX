package com.orvix.smarthome

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BackupSettings(
    val theme: String, val lang: String,
    val voiceOn: Boolean, val voiceDur: Int, val voiceLang: String, val voiceReply: Boolean
)

object BackupManager {
    private const val FORMAT = "orvix-backup"

    private fun JSONArray.objs(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

    suspend fun buildJson(ctx: Context, st: BackupSettings?): String = withContext(Dispatchers.IO) {
        val dao = OrvixDatabase.get(ctx).dao()
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", 1)

        val rooms = JSONArray()
        dao.observeRooms().first().forEach { r ->
            rooms.put(JSONObject().put("id", r.id).put("name", r.name).put("pinned", r.pinned))
        }
        root.put("rooms", rooms)

        val devices = JSONArray()
        dao.observeDevices().first().forEach { d ->
            val o = JSONObject()
                .put("id", d.id).put("roomId", d.roomId).put("name", d.name)
                .put("type", d.type).put("host", d.host).put("port", d.port)
                .put("checkMessage", d.checkMessage)
            if (d.gatewayId != null) o.put("gatewayId", d.gatewayId)
            devices.put(o)
        }
        root.put("devices", devices)

        val relays = JSONArray()
        dao.observeRelays().first().forEach { r ->
            relays.put(
                JSONObject()
                    .put("id", r.id).put("deviceId", r.deviceId).put("name", r.name)
                    .put("onMessage", r.onMessage).put("offMessage", r.offMessage)
                    .put("isOn", r.isOn).put("icon", r.icon)
                    .put("topic", r.topic).put("alias", r.alias)
            )
        }
        root.put("relays", relays)

        val sensors = JSONArray()
        dao.observeSensors().first().forEach { x ->
            sensors.put(
                JSONObject()
                    .put("id", x.id).put("deviceId", x.deviceId).put("name", x.name)
                    .put("type", x.type).put("config", x.config)
            )
        }
        root.put("sensors", sensors)

        val mqtt = JSONArray()
        dao.observeMqtt().first().forEach { m ->
            mqtt.put(
                JSONObject()
                    .put("id", m.id).put("deviceId", m.deviceId).put("name", m.name)
                    .put("clientId", m.clientId).put("broker", m.broker).put("port", m.port)
                    .put("tls", m.tls).put("username", m.username)
            )
        }
        root.put("mqtt", mqtt)

        if (st != null) {
            root.put(
                "settings",
                JSONObject()
                    .put("theme", st.theme).put("lang", st.lang)
                    .put("voiceOn", st.voiceOn).put("voiceDur", st.voiceDur)
                    .put("voiceLang", st.voiceLang).put("voiceReply", st.voiceReply)
            )
        }
        root.toString(2)
    }

    suspend fun restore(ctx: Context, json: String): BackupSettings? = withContext(Dispatchers.IO) {
        val root = JSONObject(json)
        require(root.optString("format") == FORMAT) { "Not an ORVIX backup file" }

        // সব আগে পার্স করা হচ্ছে, ভুল ফাইল হলে এখানেই থেমে যাবে, ডাটা মুছবে না
        val rooms = root.getJSONArray("rooms").objs().map {
            RoomEntity(it.getLong("id"), it.getString("name"), it.optBoolean("pinned", false))
        }
        val devices = root.getJSONArray("devices").objs().map {
            DeviceEntity(
                id = it.getLong("id"), roomId = it.getLong("roomId"), name = it.getString("name"),
                type = it.getString("type"), host = it.getString("host"), port = it.getInt("port"),
                gatewayId = if (it.has("gatewayId")) it.getLong("gatewayId") else null,
                checkMessage = it.optString("checkMessage", "")
            )
        }
        val relays = root.getJSONArray("relays").objs().map {
            RelayEntity(
                id = it.getLong("id"), deviceId = it.getLong("deviceId"), name = it.getString("name"),
                onMessage = it.getString("onMessage"), offMessage = it.getString("offMessage"),
                isOn = it.optBoolean("isOn", false), icon = it.optString("icon", "light"),
                topic = it.optString("topic", ""), alias = it.optString("alias", "")
            )
        }
        val sensors = root.getJSONArray("sensors").objs().map {
            SensorEntity(
                id = it.getLong("id"), deviceId = it.getLong("deviceId"), name = it.getString("name"),
                type = it.getString("type"), config = it.getString("config")
            )
        }
        val mqtt = root.getJSONArray("mqtt").objs().map {
            MqttConnEntity(
                id = it.getLong("id"), deviceId = it.getLong("deviceId"), name = it.getString("name"),
                clientId = it.getString("clientId"), broker = it.getString("broker"),
                port = it.getInt("port"), tls = it.getBoolean("tls"), username = it.getString("username")
            )
        }

        // রিস্টোরের আগে বর্তমান অবস্থার একটা নিরাপত্তা কপি অ্যাপের ভেতরে রাখা হচ্ছে
        File(ctx.filesDir, "orvix_before_restore.json").writeText(buildJson(ctx, null))

        val db = OrvixDatabase.get(ctx)
        val dao = db.dao()
        db.clearAllTables()
        db.withTransaction {
            rooms.forEach { dao.insertRoom(it) }
            devices.forEach { dao.insertDevice(it) }
            if (relays.isNotEmpty()) dao.insertRelays(relays)
            sensors.forEach { dao.insertSensor(it) }
            mqtt.forEach { dao.upsertMqtt(it) }
        }

        root.optJSONObject("settings")?.let {
            BackupSettings(
                it.optString("theme", "DARK"), it.optString("lang", "EN"),
                it.optBoolean("voiceOn", false), it.optInt("voiceDur", 1),
                it.optString("voiceLang", "EN"), it.optBoolean("voiceReply", true)
            )
        }
    }
}
