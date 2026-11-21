package com.orvix.smarthome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrvixViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = OrvixRepository(OrvixDatabase.get(app).dao())

    val rooms: StateFlow<List<RoomEntity>> =
        repo.rooms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val devices: StateFlow<List<DeviceEntity>> =
        repo.devices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val relays: StateFlow<List<RelayEntity>> =
        repo.relays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sensors: StateFlow<List<SensorEntity>> =
        repo.sensors.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mqttConns: StateFlow<List<MqttConnEntity>> =
        repo.mqtt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRoom(name: String) { viewModelScope.launch { repo.addRoom(name) } }
    fun renameRoom(room: RoomEntity, name: String) { viewModelScope.launch { repo.renameRoom(room, name) } }
    fun deleteRoom(room: RoomEntity) { viewModelScope.launch { repo.deleteRoom(room) } }
    fun setRoomPinned(room: RoomEntity, pinned: Boolean) { viewModelScope.launch { repo.setRoomPinned(room, pinned) } }

    fun addDevice(device: DeviceEntity) { viewModelScope.launch { repo.addDevice(device) } }
    fun updateDevice(device: DeviceEntity) { viewModelScope.launch { repo.updateDevice(device) } }
    fun deleteDevice(device: DeviceEntity) { viewModelScope.launch { repo.deleteDevice(device); Secrets.put(getApplication<Application>(), "mqtt_pw_" + device.id, "") } }

    fun addRelays(list: List<RelayEntity>) { viewModelScope.launch { repo.addRelays(list) } }
    fun updateRelay(relay: RelayEntity) { viewModelScope.launch { repo.updateRelay(relay) } }
    fun deleteRelay(relay: RelayEntity) { viewModelScope.launch { repo.deleteRelay(relay) } }
    fun setAllRelays(deviceId: Long, on: Boolean) { viewModelScope.launch { repo.setAllRelays(deviceId, on) } }

    fun addSensor(sensor: SensorEntity) { viewModelScope.launch { repo.addSensor(sensor) } }
    fun updateSensor(sensor: SensorEntity) { viewModelScope.launch { repo.updateSensor(sensor) } }
    fun deleteSensor(sensor: SensorEntity) { viewModelScope.launch { repo.deleteSensor(sensor) } }
    fun saveMqtt(conn: MqttConnEntity) { viewModelScope.launch { repo.saveMqtt(conn) } }
    fun deleteMqtt(conn: MqttConnEntity) { viewModelScope.launch { repo.deleteMqtt(conn.deviceId) } }
    fun addMqttDevice(device: DeviceEntity, conn: MqttConnEntity, password: String) {
        viewModelScope.launch {
            val id = repo.addMqttDevice(device, conn)
            Secrets.put(getApplication<Application>(), "mqtt_pw_" + id, password)
        }
    }
    fun updateMqttDevice(device: DeviceEntity, conn: MqttConnEntity) {
        viewModelScope.launch { repo.updateDevice(device); repo.saveMqtt(conn) }
    }
}
