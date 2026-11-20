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

    fun addRoom(name: String) {
        viewModelScope.launch { repo.addRoom(name) }
    }

    fun renameRoom(room: RoomEntity, name: String) {
        viewModelScope.launch { repo.renameRoom(room, name) }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch { repo.deleteRoom(room) }
    }

    fun addDevice(device: DeviceEntity) {
        viewModelScope.launch { repo.addDevice(device) }
    }

    fun updateDevice(device: DeviceEntity) {
        viewModelScope.launch { repo.updateDevice(device) }
    }

    fun deleteDevice(device: DeviceEntity) {
        viewModelScope.launch { repo.deleteDevice(device) }
    }
}
