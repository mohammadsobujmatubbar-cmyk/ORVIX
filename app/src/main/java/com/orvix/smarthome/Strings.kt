package com.orvix.smarthome

enum class Lang { EN, BN }

val SensorTypes = listOf("Temperature", "Humidity", "PIR", "Voltage", "Battery", "Other")

data class Strings(
    val welcome: String, val home: String, val rooms: String, val devices: String, val settings: String,
    val all: String, val totalRooms: String, val controllers: String, val relays: String,
    val systemStatus: String, val appReady: String, val notConfigured: String,
    val theme: String, val themeSystem: String, val themeDark: String, val themeLight: String,
    val language: String,
    val bgTitle: String, val bgChoose: String, val bgRemove: String, val bgDimLow: String, val bgDimMid: String, val bgDimHigh: String,
    val addRoom: String, val renameRoom: String, val roomName: String,
    val addDevice: String, val editDevice: String, val deviceName: String, val deviceType: String,
    val hostLabel: String, val portLabel: String, val roomLabel: String,
    val save: String, val cancel: String, val delete: String, val edit: String,
    val noControllers: String, val noRooms: String,
    val deleteRoomMsg: String, val deleteDeviceMsg: String,
    val invalidPort: String, val controlNote: String,
    val on: String, val off: String, val open: String, val back: String,
    val sensorsTitle: String, val addRelay: String, val editRelay: String, val relayName: String,
    val onMessage: String, val offMessage: String, val countLabel: String, val countHint: String,
    val allOn: String, val allOff: String,
    val addSensor: String, val editSensor: String, val sensorName: String, val sensorType: String,
    val sensorConfig: String, val noRelays: String, val noSensors: String,
    val deleteRelayMsg: String, val deleteSensorMsg: String, val notSet: String,
    val tempLabel: String, val humidityLabel: String, val pirLabel: String,
    val voltageLabel: String, val batteryLabel: String, val otherLabel: String
)

private val en = Strings(
    welcome = "Welcome Home", home = "Home", rooms = "Rooms", devices = "Devices", settings = "Settings",
    all = "All", totalRooms = "Rooms", controllers = "Controllers", relays = "Relays",
    systemStatus = "System Status", appReady = "App ready", notConfigured = "Device connection: Not configured",
    theme = "Theme", themeSystem = "System", themeDark = "Dark", themeLight = "Light",
    language = "Language",
    bgTitle = "Background", bgChoose = "Choose photo", bgRemove = "Remove photo", bgDimLow = "Dim: Low", bgDimMid = "Dim: Mid", bgDimHigh = "Dim: High",
    addRoom = "Add Room", renameRoom = "Rename Room", roomName = "Room name",
    addDevice = "Add Device", editDevice = "Edit Device", deviceName = "Device name", deviceType = "Type",
    hostLabel = "IP / Host", portLabel = "Port", roomLabel = "Room",
    save = "Save", cancel = "Cancel", delete = "Delete", edit = "Edit",
    noControllers = "No devices yet. Add one from the Devices tab.",
    noRooms = "Add a room first.",
    deleteRoomMsg = "Deleting this room also deletes its devices.",
    deleteDeviceMsg = "Delete this device? Its relays and sensors are deleted too.",
    invalidPort = "Port must be 1-65535",
    controlNote = "Messages are stored only. Nothing is sent to the device yet (UDP arrives in the next phase).",
    on = "On", off = "Off", open = "Open", back = "Back",
    sensorsTitle = "Sensors", addRelay = "Add Relay", editRelay = "Edit Relay", relayName = "Relay name",
    onMessage = "ON message", offMessage = "OFF message", countLabel = "How many relays (1-32)",
    countHint = "Messages can be set for each relay after adding.",
    allOn = "All ON", allOff = "All OFF",
    addSensor = "Add Sensor", editSensor = "Edit Sensor", sensorName = "Sensor name", sensorType = "Type",
    sensorConfig = "Command / reading config (optional)",
    noRelays = "No relays yet.", noSensors = "No sensors yet.",
    deleteRelayMsg = "Delete this relay?", deleteSensorMsg = "Delete this sensor?", notSet = "not set",
    tempLabel = "Temperature", humidityLabel = "Humidity", pirLabel = "PIR / Motion",
    voltageLabel = "Voltage", batteryLabel = "Battery", otherLabel = "Other"
)

private val bn = Strings(
    welcome = "স্বাগতম, আপনার বাসায়", home = "হোম", rooms = "রুম", devices = "ডিভাইস", settings = "সেটিংস",
    all = "সব", totalRooms = "রুম", controllers = "কন্ট্রোলার", relays = "রিলে",
    systemStatus = "সিস্টেম স্ট্যাটাস", appReady = "অ্যাপ প্রস্তুত", notConfigured = "ডিভাইস সংযোগ: কনফিগার করা হয়নি",
    theme = "থিম", themeSystem = "সিস্টেম", themeDark = "ডার্ক", themeLight = "লাইট",
    language = "ভাষা",
    bgTitle = "ব্যাকগ্রাউন্ড", bgChoose = "ছবি বাছাই", bgRemove = "ছবি সরান", bgDimLow = "আবছা: কম", bgDimMid = "আবছা: মাঝারি", bgDimHigh = "আবছা: বেশি",
    addRoom = "রুম যোগ করুন", renameRoom = "রুমের নাম পরিবর্তন", roomName = "রুমের নাম",
    addDevice = "ডিভাইস যোগ করুন", editDevice = "ডিভাইস সম্পাদনা", deviceName = "ডিভাইসের নাম", deviceType = "ধরন",
    hostLabel = "IP / হোস্ট", portLabel = "পোর্ট", roomLabel = "রুম",
    save = "সংরক্ষণ", cancel = "বাতিল", delete = "মুছুন", edit = "সম্পাদনা",
    noControllers = "এখনো কোনো ডিভাইস যোগ করা হয়নি। ডিভাইস ট্যাব থেকে যোগ করুন।",
    noRooms = "আগে একটি রুম যোগ করুন।",
    deleteRoomMsg = "এই রুম মুছলে এর সব ডিভাইসও মুছে যাবে।",
    deleteDeviceMsg = "এই ডিভাইস মুছে ফেলবেন? এর রিলে ও সেন্সরও মুছে যাবে।",
    invalidPort = "পোর্ট ১ থেকে ৬৫৫৩৫-এর মধ্যে হতে হবে",
    controlNote = "মেসেজগুলো শুধু সংরক্ষিত থাকে। এখনো ডিভাইসে কিছু পাঠানো হয় না (UDP পরের ধাপে আসবে)।",
    on = "চালু", off = "বন্ধ", open = "খুলুন", back = "পিছনে",
    sensorsTitle = "সেন্সর", addRelay = "রিলে যোগ করুন", editRelay = "রিলে সম্পাদনা", relayName = "রিলের নাম",
    onMessage = "ON মেসেজ", offMessage = "OFF মেসেজ", countLabel = "কয়টি রিলে (১-৩২)",
    countHint = "যোগ করার পর প্রতিটি রিলের মেসেজ আলাদাভাবে সেট করা যাবে।",
    allOn = "সব ON", allOff = "সব OFF",
    addSensor = "সেন্সর যোগ করুন", editSensor = "সেন্সর সম্পাদনা", sensorName = "সেন্সরের নাম", sensorType = "ধরন",
    sensorConfig = "কমান্ড / রিডিং কনফিগ (ঐচ্ছিক)",
    noRelays = "এখনো কোনো রিলে নেই।", noSensors = "এখনো কোনো সেন্সর নেই।",
    deleteRelayMsg = "এই রিলে মুছে ফেলবেন?", deleteSensorMsg = "এই সেন্সর মুছে ফেলবেন?", notSet = "সেট করা হয়নি",
    tempLabel = "তাপমাত্রা", humidityLabel = "আর্দ্রতা", pirLabel = "PIR / মোশন",
    voltageLabel = "ভোল্টেজ", batteryLabel = "ব্যাটারি", otherLabel = "অন্যান্য"
)

fun strings(lang: Lang): Strings = if (lang == Lang.BN) bn else en

fun sensorTypeLabel(s: Strings, type: String): String = when (type) {
    "Temperature" -> s.tempLabel
    "Humidity" -> s.humidityLabel
    "PIR" -> s.pirLabel
    "Voltage" -> s.voltageLabel
    "Battery" -> s.batteryLabel
    else -> s.otherLabel
}
