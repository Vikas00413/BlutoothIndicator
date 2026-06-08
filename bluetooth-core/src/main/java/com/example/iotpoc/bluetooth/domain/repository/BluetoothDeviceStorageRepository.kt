package com.example.iotpoc.bluetooth.domain.repository

import com.example.iotpoc.bluetooth.domain.model.SavedBluetoothDevice
import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic storage contract.
 * Android implementation uses DataStore; future KMP target may use SQLDelight/DataStore multiplatform.
 */
interface BluetoothDeviceStorageRepository {
    val savedDevices: Flow<List<SavedBluetoothDevice>>

    suspend fun saveDevice(device: SavedBluetoothDevice)

    suspend fun removeDevice(address: String)

    suspend fun getLastConnectedAddress(): String?

    suspend fun clearAll()
}
