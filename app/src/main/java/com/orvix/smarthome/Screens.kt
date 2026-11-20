package com.orvix.smarthome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DemoDevice(val id: Int, val roomIndex: Int, val name: String)

@Composable
fun HomeScreen(
    s: Strings, rooms: List<String>, devices: List<DemoDevice>,
    states: MutableMap<Int, Boolean>, modifier: Modifier = Modifier
) {
    val g = LocalGlass.current
    var selected by remember { mutableStateOf(-1) }
    val shown = if (selected < 0) devices else devices.filter { it.roomIndex == selected }
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
            item { GlassChip(s.all, selected < 0) { selected = -1 } }
            itemsIndexed(rooms) { i, r -> GlassChip(r, selected == i) { selected = i } }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(s.totalRooms, rooms.size.toString(), Modifier.weight(1f))
            StatCard(s.controllers, "0", Modifier.weight(1f))
            StatCard(s.relays, "0", Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        shown.chunked(2).forEach { pair ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { d ->
                    DeviceCard(
                        room = rooms[d.roomIndex], name = d.name,
                        isOn = states[d.id] == true, onLabel = s.on, offLabel = s.off,
                        onToggle = { states[d.id] = states[d.id] != true },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text(s.sampleNote, color = g.subText, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text(s.systemStatus, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(s.appReady, color = g.subText)
            Text(s.notConfigured, color = g.subText)
        }
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
fun RoomsScreen(s: Strings, rooms: List<String>, modifier: Modifier = Modifier) {
    val g = LocalGlass.current
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(s.rooms, color = g.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        rooms.forEach { r ->
            GlassCard(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(r, color = g.text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Text("0 " + s.controllers, color = g.subText, fontSize = 13.sp)
            }
        }
        Text(s.roomsNote, color = g.subText, fontSize = 13.sp)
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
fun DevicesScreen(s: Strings, modifier: Modifier = Modifier) {
    val g = LocalGlass.current
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(s.devices, color = g.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text(s.devicesEmpty, color = g.subText)
        }
        Spacer(Modifier.height(120.dp))
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
fun OrvixApp(lang: Lang, mode: ThemeMode, onTheme: (ThemeMode) -> Unit, onLang: (Lang) -> Unit) {
    val s = strings(lang)
    val g = LocalGlass.current
    var tab by remember { mutableStateOf(0) }
    val states = remember { mutableStateMapOf<Int, Boolean>() }
    val rooms = listOf(s.bedroom, s.livingRoom, s.kitchen, s.diningRoom)
    val devices = listOf(
        DemoDevice(1, 1, s.sampleLight),
        DemoDevice(2, 1, s.sampleFan),
        DemoDevice(3, 2, s.sampleLight),
        DemoDevice(4, 0, s.sampleFan)
    )
    val items = listOf(
        NavItem(Icons.Default.Home, s.home),
        NavItem(Icons.Default.Place, s.rooms),
        NavItem(Icons.Default.Menu, s.devices),
        NavItem(Icons.Default.Settings, s.settings)
    )
    Box(Modifier.fillMaxSize().background(g.background).statusBarsPadding()) {
        val full = Modifier.fillMaxSize()
        when (tab) {
            0 -> HomeScreen(s, rooms, devices, states, full)
            1 -> RoomsScreen(s, rooms, full)
            2 -> DevicesScreen(s, full)
            else -> SettingsScreen(s, lang, mode, onLang, onTheme, full)
        }
        GlassBottomBar(
            items, tab, { tab = it },
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 16.dp)
        )
    }
}
