package com.orvix.smarthome

enum class Lang { EN, BN }

data class Strings(
    val welcome: String, val home: String, val rooms: String, val devices: String, val settings: String,
    val all: String, val totalRooms: String, val controllers: String, val relays: String,
    val systemStatus: String, val appReady: String, val notConfigured: String,
    val theme: String, val themeSystem: String, val themeDark: String, val themeLight: String,
    val language: String,
    val addRoom: String, val renameRoom: String, val roomName: String,
    val addDevice: String, val editDevice: String, val deviceName: String, val deviceType: String,
    val hostLabel: String, val portLabel: String, val roomLabel: String,
    val save: String, val cancel: String, val delete: String, val edit: String,
    val noControllers: String, val noRooms: String,
    val deleteRoomMsg: String, val deleteDeviceMsg: String,
    val invalidPort: String, val controlNote: String
)

private val en = Strings(
    welcome = "Welcome Home", home = "Home", rooms = "Rooms", devices = "Devices", settings = "Settings",
    all = "All", totalRooms = "Rooms", controllers = "Controllers", relays = "Relays",
    systemStatus = "System Status", appReady = "App ready", notConfigured = "Device connection: Not configured",
    theme = "Theme", themeSystem = "System", themeDark = "Dark", themeLight = "Light",
    language = "Language",
    addRoom = "Add Room", renameRoom = "Rename Room", roomName = "Room name",
    addDevice = "Add Device", editDevice = "Edit Device", deviceName = "Device name", deviceType = "Type",
    hostLabel = "IP / Host", portLabel = "Port", roomLabel = "Room",
    save = "Save", cancel = "Cancel", delete = "Delete", edit = "Edit",
    noControllers = "No devices yet. Add one from the Devices tab.",
    noRooms = "Add a room first.",
    deleteRoomMsg = "Deleting this room also deletes its devices.",
    deleteDeviceMsg = "Delete this device?",
    invalidPort = "Port must be 1-65535",
    controlNote = "Relays and control panels arrive in the next step."
)

private val bn = Strings(
    welcome = "স্বাগতম, আপনার বাসায়", home = "হোম", rooms = "রুম", devices = "ডিভাইস", settings = "সেটিংস",
    all = "সব", totalRooms = "রুম", controllers = "কন্ট্রোলার", relays = "রিলে",
    systemStatus = "সিস্টেম স্ট্যাটাস", appReady = "অ্যাপ প্রস্তুত", notConfigured = "ডিভাইস সংযোগ: কনফিগার করা হয়নি",
    theme = "থিম", themeSystem = "সিস্টেম", themeDark = "ডার্ক", themeLight = "লাইট",
    language = "ভাষা",
    addRoom = "রুম যোগ করুন", renameRoom = "রুমের নাম পরিবর্তন", roomName = "রুমের নাম",
    addDevice = "ডিভাইস যোগ করুন", editDevice = "ডিভাইস সম্পাদনা", deviceName = "ডিভাইসের নাম", deviceType = "ধরন",
    hostLabel = "IP / হোস্ট", portLabel = "পোর্ট", roomLabel = "রুম",
    save = "সংরক্ষণ", cancel = "বাতিল", delete = "মুছুন", edit = "সম্পাদনা",
    noControllers = "এখনো কোনো ডিভাইস যোগ করা হয়নি। ডিভাইস ট্যাব থেকে যোগ করুন।",
    noRooms = "আগে একটি রুম যোগ করুন।",
    deleteRoomMsg = "এই রুম মুছলে এর সব ডিভাইসও মুছে যাবে।",
    deleteDeviceMsg = "এই ডিভাইস মুছে ফেলবেন?",
    invalidPort = "পোর্ট ১ থেকে ৬৫৫৩৫-এর মধ্যে হতে হবে",
    controlNote = "রিলে ও কন্ট্রোল প্যানেল পরের ধাপে আসবে।"
)

fun strings(lang: Lang): Strings = if (lang == Lang.BN) bn else en
