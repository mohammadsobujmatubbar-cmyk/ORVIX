package com.orvix.smarthome

sealed class VoiceParse {
    object NoMatch : VoiceParse()
    object Ambiguous : VoiceParse()
    class Act(
        val targets: List<Pair<DeviceEntity, RelayEntity>>,
        val on: Boolean,
        val all: Boolean
    ) : VoiceParse()
}

private val onWords = setOf("on", "start", "চালু", "চালাও", "অন", "জ্বালাও", "জ্বালিয়ে", "জ্বেলে", "চালান", "চালিয়ে")
private val offWords = setOf("off", "stop", "বন্ধ", "অফ", "নিভাও", "নেভাও", "নিভিয়ে", "নিভান", "নিভে")
private val allWords = setOf("all", "everything", "সব", "সকল", "সবগুলো", "সবগুলি", "সবকিছু")
private val iconWords = mapOf(
    "light" to setOf("light", "lights", "lamp", "bulb", "লাইট", "বাতি", "আলো", "ল্যাম্প", "বালব"),
    "fan" to setOf("fan", "fans", "ফ্যান", "পাখা"),
    "tv" to setOf("tv", "television", "টিভি", "টেলিভিশন"),
    "ac" to setOf("ac", "aircon", "এসি", "এয়ারকন্ডিশনার"),
    "plug" to setOf("plug", "socket", "প্লাগ", "সকেট")
)
private val splitter = Regex("[\\s,.!?।;:_-]+")

private fun toks(t: String): List<String> =
    t.lowercase().split(splitter).filter { it.isNotBlank() }

fun parseVoice(
    text: String,
    rooms: List<RoomEntity>,
    devices: List<DeviceEntity>,
    relays: List<RelayEntity>
): VoiceParse {
    val ws = toks(text)
    val set = ws.toSet()
    val hasOn = ws.any { it in onWords }
    val hasOff = ws.any { it in offWords }
    if (hasOn == hasOff) return VoiceParse.NoMatch
    val turnOn = hasOn
    val devById = devices.associateBy { it.id }
    val spokenRooms = rooms.filter { r ->
        val t = toks(r.name)
        t.isNotEmpty() && t.all { it in set }
    }
    val spokenDevs = devices.filter { d ->
        val t = toks(d.name)
        t.isNotEmpty() && t.all { it in set }
    }

    fun finish(cands: List<RelayEntity>, all: Boolean): VoiceParse {
        var c = cands
        val byDev = c.filter { r -> spokenDevs.any { it.id == r.deviceId } }
        if (byDev.isNotEmpty()) {
            c = byDev
        } else if (spokenRooms.isNotEmpty()) {
            val byRoom = c.filter { r ->
                val d = devById[r.deviceId]
                d != null && spokenRooms.any { it.id == d.roomId }
            }
            if (byRoom.isEmpty()) return VoiceParse.NoMatch
            c = byRoom
        }
        if (!all && c.map { it.deviceId }.toSet().size > 1) return VoiceParse.Ambiguous
        val pairs = c.mapNotNull { r -> devById[r.deviceId]?.let { d -> Pair(d, r) } }
        if (pairs.isEmpty()) return VoiceParse.NoMatch
        return VoiceParse.Act(pairs, turnOn, all)
    }

    var best = 0
    val named = ArrayList<RelayEntity>()
    for (r in relays) {
        val names = ArrayList<String>()
        names.add(r.name)
        for (a in r.alias.split(",")) {
            if (a.isNotBlank()) names.add(a)
        }
        var score = 0
        for (n in names) {
            val t = toks(n)
            if (t.isNotEmpty() && t.all { it in set } && t.size > score) score = t.size
        }
        if (score > 0) {
            if (score > best) {
                best = score
                named.clear()
            }
            if (score == best) named.add(r)
        }
    }
    if (named.isNotEmpty()) return finish(named, false)

    val byIcon = relays.filter { r ->
        val w = iconWords[r.icon]
        w != null && w.any { it in set }
    }
    if (byIcon.isNotEmpty()) return finish(byIcon, false)

    if (ws.any { it in allWords }) return finish(relays, true)
    return VoiceParse.NoMatch
}
