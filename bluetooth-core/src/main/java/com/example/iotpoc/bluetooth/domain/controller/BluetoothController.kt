package com.example.iotpoc.bluetooth.domain.controller

import com.example.iotpoc.bluetooth.domain.model.BluetoothConnectionState
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogEntry
import com.example.iotpoc.bluetooth.domain.model.DiscoveredBluetoothDevice
import com.example.iotpoc.bluetooth.domain.model.SavedBluetoothDevice
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Primary library API consumed by the app UI layer.
 * Implementation delegates to Foreground Service + BLE engine on Android.
 */
interface BluetoothController {
    val connectionState: StateFlow<BluetoothConnectionState>
    val connectedAddress: StateFlow<String?>
    val savedDevices: StateFlow<List<SavedBluetoothDevice>>
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>>
    val logs: SharedFlow<BluetoothLogEntry>
    val isScanning: StateFlow<Boolean>
    val isGattReady: StateFlow<Boolean>
    val receivedMessages: StateFlow<List<String>>
    val lastError: StateFlow<String?>

    suspend fun startForegroundSession()

    suspend fun stopForegroundSession()

    suspend fun startScan()

    suspend fun stopScan()

    suspend fun connect(address: String, displayName: String? = null)

    suspend fun disconnect()

    suspend fun reconnectLastDevice()

    suspend fun sendText(text: String)

    fun clearDiscoveredDevices()

    fun clearMessages()
}
