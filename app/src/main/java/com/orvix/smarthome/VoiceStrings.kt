package com.orvix.smarthome

data class VoiceStrings(
    val title: String, val on: String, val off: String,
    val duration: String, val d30: String, val d1: String, val d5: String, val dOpen: String,
    val language: String, val note: String,
    val enableFirst: String, val needPerm: String, val unavailable: String,
    val listening: String, val heardLabel: String, val noMatch: String, val ambiguous: String,
    val stopped: String, val timeUp: String, val allDone: String, val error: String,
    val reply: String, val sorry: String, val ambiguousSpoken: String, val ttsMissing: String
)

private val enV = VoiceStrings(
    title = "Voice control", on = "On", off = "Off",
    duration = "Stay on for", d30 = "30 sec", d1 = "1 min", d5 = "5 min", dOpen = "Until I stop",
    language = "Voice language",
    note = "Tap the mic in the bottom bar and say: \"LED on\", \"fan off\", \"all off\". Works while the app is open.",
    enableFirst = "Turn on Voice in Settings first",
    needPerm = "Microphone permission is needed",
    unavailable = "Speech recognition is not available on this phone",
    listening = "Listening...", heardLabel = "Heard",
    noMatch = "No matching relay",
    ambiguous = "More than one match. Say the room or device name",
    stopped = "Voice stopped", timeUp = "Voice time is up", allDone = "All",
    error = "Voice error",
    reply = "Spoken reply",
    sorry = "I'm sorry, I didn't understand",
    ambiguousSpoken = "More than one match. Please say the room or device name",
    ttsMissing = "Spoken reply is not available for this language on this phone"
)

private val bnV = VoiceStrings(
    title = "ভয়েস কন্ট্রোল", on = "চালু", off = "বন্ধ",
    duration = "কতক্ষণ চালু থাকবে", d30 = "৩০ সেকেন্ড", d1 = "১ মিনিট", d5 = "৫ মিনিট", dOpen = "আমি বন্ধ করা পর্যন্ত",
    language = "ভয়েসের ভাষা",
    note = "নিচের বারের মাইক চেপে বলুন: \"LED on\", \"ফ্যান বন্ধ\", \"সব বন্ধ\"। অ্যাপ খোলা থাকলে কাজ করে।",
    enableFirst = "আগে সেটিংসে ভয়েস চালু করুন",
    needPerm = "মাইক্রোফোনের অনুমতি লাগবে",
    unavailable = "এই ফোনে ভয়েস চেনার সুবিধা নেই",
    listening = "শুনছি...", heardLabel = "শুনেছি",
    noMatch = "মেলে এমন রিলে নেই",
    ambiguous = "একাধিক মিল পাওয়া গেছে। রুম বা ডিভাইসের নাম বলুন",
    stopped = "ভয়েস বন্ধ", timeUp = "ভয়েসের সময় শেষ", allDone = "সব",
    error = "ভয়েসে সমস্যা",
    reply = "ভয়েসে উত্তর",
    sorry = "দুঃখিত, আমি কিছু বুঝতে পারছি না",
    ambiguousSpoken = "একাধিক মিল পাওয়া গেছে। রুম বা ডিভাইসের নাম বলুন",
    ttsMissing = "এই ফোনে এই ভাষায় কথা বলার সুবিধা নেই"
)

fun voiceStrings(lang: Lang): VoiceStrings = if (lang == Lang.BN) bnV else enV
