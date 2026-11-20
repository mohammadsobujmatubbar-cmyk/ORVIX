package com.orvix.smarthome

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    s: Strings, rooms: List<RoomEntity>, devices: List<DeviceEntity>, relayCount: Int,
    modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    var selectedId by remember { mutableStateOf(-1L) }
    val sel = if (rooms.any { it.id == selectedId }) selectedId else -1L
    val shown = if (sel < 0) devices else devices.filter { it.roomId == sel }
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            RoundIcon(Icons.Default.Notifications, "Notifications")
            RoundIcon(Icons.Default.Add, "Add")
        }
        Spacer(Modifier.height(20.dp))
        Text(s.welcome, color = g.text, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
        Spacer(Modifier.height(20.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            item { GlassChip(s.all, sel < 0) { selectedId = -1L } }
            items(rooms, key = { it.id }) { r -> GlassChip(r.name, sel == r.id) { selectedId = r.id } }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(s.totalRooms, rooms.size.toString(), Modifier.weight(1f))
            StatCard(s.controllers, devices.size.toString(), Modifier.weight(1f))
            StatCard(s.relays, relayCount.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        shown.chunked(2).forEach { pair ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { d ->
                    GlassCard(Modifier.weight(1f)) {
                        Text(
                            rooms.firstOrNull { it.id == d.roomId }?.name ?: "",
                            color = g.subText, fontSize = 12.sp
                        )
                        Text(d.name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        Text(d.type, color = g.subText, fontSize = 13.sp)
                        Text(d.host + ":" + d.port, color = g.subText, fontSize = 13.sp)
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (shown.isEmpty()) {
            Text(s.noControllers, color = g.subText, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
        }
        GlassCard(Modifier.fillMaxWidth()) {
            Text(s.systemStatus, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(s.appReady, color = g.subText)
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
fun RoomsScreen(
    s: Strings, rooms: List<RoomEntity>, devices: List<DeviceEntity>,
    onAdd: (String) -> Unit, onRename: (RoomEntity, String) -> Unit, onDelete: (RoomEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RoomEntity?>(null) }
    var deleting by remember { mutableStateOf<RoomEntity?>(null) }
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(s.rooms, color = g.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            GlassButton("+ " + s.addRoom) { showAdd = true }
        }
        Spacer(Modifier.height(16.dp))
        rooms.forEach { r ->
            GlassCard(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(r.name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(
                    devices.count { it.roomId == r.id }.toString() + " " + s.controllers,
                    color = g.subText, fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(s.edit, false) { editing = r }
                    GlassChip(s.delete, false) { deleting = r }
                }
            }
        }
        Spacer(Modifier.height(120.dp))
    }
    if (showAdd) {
        RoomDialog(s, s.addRoom, "", { showAdd = false }) { onAdd(it); showAdd = false }
    }
    editing?.let { r ->
        RoomDialog(s, s.renameRoom, r.name, { editing = null }) { onRename(r, it); editing = null }
    }
    deleting?.let { r ->
        ConfirmDialog(s.delete, s.deleteRoomMsg, s.delete, s.cancel, { onDelete(r); deleting = null }, { deleting = null })
    }
}

@Composable
fun DevicesScreen(
    s: Strings, rooms: List<RoomEntity>, devices: List<DeviceEntity>, relays: List<RelayEntity>,
    onOpen: (DeviceEntity) -> Unit,
    onAdd: (DeviceEntity) -> Unit, onUpdate: (DeviceEntity) -> Unit, onDelete: (DeviceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<DeviceEntity?>(null) }
    var deleting by remember { mutableStateOf<DeviceEntity?>(null) }
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(s.devices, color = g.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            GlassButton("+ " + s.addDevice) { showAdd = true }
        }
        Spacer(Modifier.height(16.dp))
        if (devices.isEmpty()) {
            GlassCard(Modifier.fillMaxWidth()) { Text(s.noControllers, color = g.subText) }
        }
        devices.forEach { d ->
            GlassCard(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    rooms.firstOrNull { it.id == d.roomId }?.name ?: "",
                    color = g.subText, fontSize = 12.sp
                )
                Text(d.name, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text(d.type + "  " + d.host + ":" + d.port, color = g.subText, fontSize = 13.sp)
                Text(
                    relays.count { it.deviceId == d.id }.toString() + " " + s.relays,
                    color = g.subText, fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                    Text(if (DeviceStatus.map.collectAsState().value[d.id] == true) "● Online" else "○ Offline", color = g.subText, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(s.open, true) { onOpen(d) }
                    GlassChip(s.edit, false) { editing = d }
                    GlassChip(s.delete, false) { deleting = d }
                }
            }
        }
        Spacer(Modifier.height(120.dp))
    }
    if (showAdd) {
        DeviceDialog(s, s.addDevice, rooms, null, { showAdd = false }) { onAdd(it); showAdd = false }
    }
    editing?.let { d ->
        DeviceDialog(s, s.editDevice, rooms, d, { editing = null }) { onUpdate(it); editing = null }
    }
    deleting?.let { d ->
        ConfirmDialog(s.delete, s.deleteDeviceMsg, s.delete, s.cancel, { onDelete(d); deleting = null }, { deleting = null })
    }
}

@Composable
fun SettingsScreen(
    s: Strings, lang: Lang, mode: ThemeMode,
    onLang: (Lang) -> Unit, onTheme: (ThemeMode) -> Unit, modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(s.settings, color = g.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text(s.theme, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(s.themeSystem, mode == ThemeMode.SYSTEM) { onTheme(ThemeMode.SYSTEM) }
                GlassChip(s.themeDark, mode == ThemeMode.DARK) { onTheme(ThemeMode.DARK) }
                GlassChip(s.themeLight, mode == ThemeMode.LIGHT) { onTheme(ThemeMode.LIGHT) }
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text(s.language, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip("English", lang == Lang.EN) { onLang(Lang.EN) }
                GlassChip("বাংলা", lang == Lang.BN) { onLang(Lang.BN) }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("ORVIX 1.0.0", color = g.subText, fontSize = 13.sp)
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
fun OrvixApp(
    lang: Lang, mode: ThemeMode, onTheme: (ThemeMode) -> Unit, onLang: (Lang) -> Unit,
    vm: OrvixViewModel
) {
    val s = strings(lang)
    val g = LocalGlass.current
    val rooms by vm.rooms.collectAsState()
    val devices by vm.devices.collectAsState()
    val relays by vm.relays.collectAsState()
    val sensors by vm.sensors.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var openId by remember { mutableStateOf<Long?>(null) }
    val openDevice = devices.firstOrNull { it.id == openId }
    BackHandler(enabled = openDevice != null) { openId = null }
    val items = listOf(
        NavItem(Icons.Default.Home, s.home),
        NavItem(Icons.Default.Place, s.rooms),
        NavItem(Icons.Default.Menu, s.devices),
        NavItem(Icons.Default.Settings, s.settings)
    )
    Box(Modifier.fillMaxSize().background(g.background).statusBarsPadding()) {
        val full = Modifier.fillMaxSize()
        if (openDevice != null) {
            DevicePanelScreen(
                s = s,
                device = openDevice,
                roomName = rooms.firstOrNull { it.id == openDevice.roomId }?.name ?: "",
                relays = relays.filter { it.deviceId == openDevice.id },
                sensors = sensors.filter { it.deviceId == openDevice.id },
                onBack = { openId = null },
                onAddRelays = vm::addRelays,
                onUpdateRelay = vm::updateRelay,
                onToggle = { r, on -> vm.devices.value.firstOrNull { it.id == r.deviceId }?.let { d -> vm.sendRelayCmd(d, r, on) } },
                onDeleteRelay = vm::deleteRelay,
                onSetAll = { on -> vm.setAllRelays(openDevice.id, on) },
                onAddSensor = vm::addSensor,
                onUpdateSensor = vm::updateSensor,
                onDeleteSensor = vm::deleteSensor,
                modifier = full.navigationBarsPadding()
            )
        } else {
            when (tab) {
                0 -> HomeScreen(s, rooms, devices, relays.size, full)
                1 -> RoomsScreen(s, rooms, devices, vm::addRoom, vm::renameRoom, vm::deleteRoom, full)
                2 -> DevicesScreen(
                    s, rooms, devices, relays, { d -> openId = d.id },
                    vm::addDevice, vm::updateDevice, vm::deleteDevice, full
                )
                else -> SettingsScreen(s, lang, mode, onLang, onTheme, full)
            }
            GlassBottomBar(
                items, tab, { tab = it },
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 16.dp)
            )
        }
    }
}
