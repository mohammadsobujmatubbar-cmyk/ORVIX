package com.orvix.smarthome

fun spokenConfirm(relays: List<RelayEntity>, on: Boolean, all: Boolean, bn: Boolean): String {
    val sir = if (bn) "স্যার" else "Sir"
    val state = if (on) (if (bn) "অন" else "on") else (if (bn) "অফ" else "off")
    if (all) return sir + ", " + (if (bn) "সব" else "all") + " " + state
    val labels = relays.map { r ->
        when (r.icon) {
            "light" -> if (bn) "লাইট" else "light"
            "fan" -> if (bn) "ফ্যান" else "fan"
            "tv" -> if (bn) "টিভি" else "TV"
            "ac" -> if (bn) "এসি" else "AC"
            "plug" -> if (bn) "প্লাগ" else "plug"
            else -> r.name
        }
    }.distinct()
    return sir + ", " + labels.joinToString(", ") + " " + state
}
