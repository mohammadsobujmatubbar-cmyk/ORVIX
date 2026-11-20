package com.orvix.smarthome

enum class Lang { EN, BN }

data class Strings(
    val welcome: String, val home: String, val rooms: String, val devices: String, val settings: String,
    val all: String, val on: String, val off: String,
    val bedroom: String, val livingRoom: String, val kitchen: String, val diningRoom: String,
    val totalRooms: String, val controllers: String, val relays: String,
    val systemStatus: String, val appReady: String, val notConfigured: String,
    val theme: String, val themeSystem: String, val themeDark: String, val themeLight: String,
    val language: String, val roomsNote: String, val devicesEmpty: String,
    val sampleLight: String, val sampleFan: String, val sampleNote: String
)

private val en = Strings(
    welcome = "Welcome Home", home = "Home", rooms = "Rooms", devices = "Devices", settings = "Settings",
    all = "All", on = "On", off = "Off",
    bedroom = "Bedroom", livingRoom = "Living Room", kitchen = "Kitchen", diningRoom = "Dining Room",
    totalRooms = "Rooms", controllers = "Controllers", relays = "Relays",
    systemStatus = "System Status", appReady = "App ready", notConfigured = "Device connection: Not configured",
    theme = "Theme", themeSystem = "System", themeDark = "Dark", themeLight = "Light",
    language = "Language",
    roomsNote = "Add, rename and delete rooms will arrive in Phase 2.",
    devicesEmpty = "No controllers added yet. Adding ESP32/ESP8266 arrives in Phase 2.",
    sampleLight = "Sample Light", sampleFan = "Sample Fan",
    sampleNote = "These cards are samples, not real devices."
)

private val bn = Strings(
    welcome = "স্বাগতম, আপনার বাসায়", home = "হোম", rooms = "রুম", devices = "ডিভাইস", settings = "সেটিংস",
    all = "সব", on = "চালু", off = "বন্ধ",
    bedroom = "বেডরুম", livingRoom = "লিভিং রুম", kitchen = "রান্নাঘর", diningRoom = "ডাইনিং রুম",
    totalRooms = "রুম", controllers = "কন্ট্রোলার", relays = "রিলে",
    systemStatus = "সিস্টেম স্ট্যাটাস", appReady = "অ্যাপ প্রস্তুত", notConfigured = "ডিভাইস সংযোগ: কনফিগার করা হয়নি",
    theme = "থিম", themeSystem = "সিস্টেম", themeDark = "ডার্ক", themeLight = "লাইট",
    language = "ভাষা",
    roomsNote = "রুম যোগ, নাম পরিবর্তন ও মুছে ফেলা Phase 2-এ আসবে।",
    devicesEmpty = "এখনো কোনো কন্ট্রোলার যোগ করা হয়নি। ESP32/ESP8266 যোগ করার সুবিধা Phase 2-এ আসবে।",
    sampleLight = "নমুনা লাইট", sampleFan = "নমুনা ফ্যান",
    sampleNote = "কার্ডগুলো শুধু নমুনা, আসল ডিভাইস নয়।"
)

fun strings(lang: Lang): Strings = if (lang == Lang.BN) bn else en
