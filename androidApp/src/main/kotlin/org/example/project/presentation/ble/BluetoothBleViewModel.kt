package org.example.project.presentation.ble

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iotpoc.bluetooth.BluetoothLibrary
import com.example.iotpoc.bluetooth.domain.controller.BluetoothController
import com.example.iotpoc.bluetooth.domain.model.BluetoothConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BluetoothBleViewModel : ViewModel() {

    companion object {
        private const val TAG = "BluetoothBleVM"
    }

    private val bluetooth: BluetoothController = BluetoothLibrary.requireController()

    private val _uiState = MutableStateFlow(BluetoothBleUiState())
    val uiState: StateFlow<BluetoothBleUiState> = _uiState.asStateFlow()

    private var foregroundSessionStarted = false

    init {
        observeBluetoothState()
    }

    private fun observeBluetoothState() {
        viewModelScope.launch {
            bluetooth.discoveredDevices.collect { discovered ->
                _uiState.update {
                    it.copy(
                        devices = discovered.map { device ->
                            DiscoveredDeviceUi(
                                address = device.address,
                                name = device.name,
                                rssi = device.rssi,
                            )
                        },
                    )
                }
            }
        }

        viewModelScope.launch {
            bluetooth.isScanning.collect { scanning ->
                _uiState.update { it.copy(isScanning = scanning) }
            }
        }

        viewModelScope.launch {
            bluetooth.connectionState.collect { connection ->
                val address = bluetooth.connectedAddress.value
                _uiState.update {
                    it.copy(
                        isConnected = connection == BluetoothConnectionState.Connected,
                        connectedAddress = if (connection == BluetoothConnectionState.Connected) {
                            address
                        } else {
                            null
                        },
                        connectingAddress = when (connection) {
                            BluetoothConnectionState.Connecting,
                            BluetoothConnectionState.Connected,
                            -> address
                            else -> null
                        },
                    )
                }
            }
        }

        viewModelScope.launch {
            bluetooth.receivedMessages.collect { messages ->
                _uiState.update { it.copy(lastMessage = messages.lastOrNull()) }
            }
        }

        viewModelScope.launch {
            bluetooth.lastError.collect { error ->
                if (error != null) {
                    _uiState.update { it.copy(errorMessage = error) }
                }
            }
        }
    }

    fun startScan() {
        viewModelScope.launch {
            runCatching {
                ensureForegroundSession()
                bluetooth.startScan()
            }.onFailure { error ->
                Log.e(TAG, "startScan failed", error)
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Failed to start scan")
                }
            }
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            runCatching { bluetooth.stopScan() }
        }
    }

    fun connect(address: String) {
        viewModelScope.launch {
            val name = _uiState.value.devices.firstOrNull { it.address == address }?.name
            runCatching {
                ensureForegroundSession()
                bluetooth.connect(address, name)
            }.onFailure { error ->
                Log.e(TAG, "connect failed", error)
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Failed to connect")
                }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            runCatching { bluetooth.disconnect() }
        }
    }

    fun clearDiscovered() {
        bluetooth.clearDiscoveredDevices()
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun ensureForegroundSession() {
        if (foregroundSessionStarted) return
        bluetooth.startForegroundSession()
        foregroundSessionStarted = true
    }

    fun resetForegroundSession() {
        foregroundSessionStarted = false
    }
}
