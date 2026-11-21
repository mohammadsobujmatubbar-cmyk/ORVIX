package com.orvix.smarthome

val genericWords = setOf("led", "light", "lamp", "bulb")

private const val BN_DIGITS = "০১২৩৪৫৬৭৮৯"

private val normMap = mapOf(
    "ডিসি" to "dc", "এসি" to "ac", "এলইডি" to "led", "টিভি" to "tv",
    "ফ্যান" to "fan", "লাইট" to "light", "বাতি" to "light",
    "ল্যাম্প" to "lamp", "বালব" to "bulb", "প্লাগ" to "plug", "সকেট" to "socket",
    "রুম" to "room", "বেডরুম" to "bedroom", "বেড" to "bed", "কিচেন" to "kitchen",
    "লিভিং" to "living", "ডাইনিং" to "dining", "ড্রয়িং" to "drawing", "অনলাইন" to "online",
    "ওয়ান" to "1", "টু" to "2", "থ্রি" to "3", "ফোর" to "4", "ফাইভ" to "5",
    "ওন" to "on", "ওফ" to "off"
)

private val suffixes = listOf("টাকে", "টিকে", "গুলো", "গুলি", "টা", "টি", "কে")

fun normTok(w: String): String {
    val d = w.map { c ->
        val i = BN_DIGITS.indexOf(c)
        if (i >= 0) ('0' + i) else c
    }.joinToString("")
    normMap[d]?.let { return it }
    for (sfx in suffixes) {
        if (d.length > sfx.length && d.endsWith(sfx)) {
            normMap[d.removeSuffix(sfx)]?.let { return it }
        }
    }
    return d
}
