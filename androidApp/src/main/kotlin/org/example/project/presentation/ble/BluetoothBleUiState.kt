package org.example.project.presentation.ble

data class DiscoveredDeviceUi(
    val address: String,
    val name: String,
    val rssi: Int,
)

data class BluetoothBleUiState(
    val devices: List<DiscoveredDeviceUi> = emptyList(),
    val isScanning: Boolean = false,
    val isConnected: Boolean = false,
    val connectedAddress: String? = null,
    val connectingAddress: String? = null,
    val errorMessage: String? = null,
    val lastMessage: String? = null,
)
