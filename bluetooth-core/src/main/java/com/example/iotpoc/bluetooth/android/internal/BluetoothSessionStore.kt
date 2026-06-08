package com.example.iotpoc.bluetooth.android.internal

import com.example.iotpoc.bluetooth.domain.model.BluetoothConnectionState
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogEntry
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogLevel
import com.example.iotpoc.bluetooth.domain.model.DiscoveredBluetoothDevice
import com.example.iotpoc.bluetooth.domain.model.SavedBluetoothDevice
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-process state bus shared between Foreground Service and UI-facing controller.
 * In KMP, this becomes a common repository with platform-specific updaters.
 */
internal object BluetoothSessionStore {
    private val _connectionState = MutableStateFlow(BluetoothConnectionState.Idle)
    val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()

    private val _connectedAddress = MutableStateFlow<String?>(null)
    val connectedAddress: StateFlow<String?> = _connectedAddress.asStateFlow()

    private val _savedDevices = MutableStateFlow<List<SavedBluetoothDevice>>(emptyList())
    val savedDevices: StateFlow<List<SavedBluetoothDevice>> = _savedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _logs = MutableSharedFlow<BluetoothLogEntry>(extraBufferCapacity = 64)
    val logs: SharedFlow<BluetoothLogEntry> = _logs.asSharedFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isGattReady = MutableStateFlow(false)
    val isGattReady: StateFlow<Boolean> = _isGattReady.asStateFlow()

    private val _receivedMessages = MutableStateFlow<List<String>>(emptyList())
    val receivedMessages: StateFlow<List<String>> = _receivedMessages.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _lastDeviceLabel = MutableStateFlow<String?>(null)
    val lastDeviceLabel: StateFlow<String?> = _lastDeviceLabel.asStateFlow()

    fun setConnectionState(state: BluetoothConnectionState) {
        _connectionState.value = state
    }

    fun setConnectedAddress(address: String?) {
        _connectedAddress.value = address
    }

    fun setSavedDevices(devices: List<SavedBluetoothDevice>) {
        _savedDevices.value = devices
    }

    fun upsertDiscovered(device: DiscoveredBluetoothDevice) {
        val map = _discoveredDevices.value.associateBy { it.address }.toMutableMap()
        map[device.address] = device
        _discoveredDevices.value = map.values.sortedBy { it.name }
    }

    fun clearDiscovered() {
        _discoveredDevices.value = emptyList()
    }

    fun setScanning(scanning: Boolean) {
        _isScanning.value = scanning
    }

    fun setGattReady(ready: Boolean) {
        _isGattReady.value = ready
    }

    fun clearMessages() {
        _receivedMessages.value = emptyList()
    }

    fun appendReceivedMessage(text: String) {
        _receivedMessages.value = (listOf(text) + _receivedMessages.value).take(50)
    }

    fun setLastError(message: String?) {
        _lastError.value = message
    }

    fun setLastDeviceLabel(label: String?) {
        _lastDeviceLabel.value = label
    }

    fun log(level: BluetoothLogLevel, tag: String, message: String) {
        _logs.tryEmit(
            BluetoothLogEntry(
                timestampEpochMs = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = message,
            )
        )
    }
}
