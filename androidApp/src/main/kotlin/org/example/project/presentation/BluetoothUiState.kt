package org.example.project.presentation

import org.example.project.domain.model.BluetoothDevice
import org.example.project.domain.model.DeviceSyncStatus

data class BluetoothUiState(
    val devices: List<BluetoothDevice> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val highlightedDeviceKey: String? = null,
    val errorMessage: String? = null,
    val saveMessage: String? = null,
    val showPendingOnly: Boolean = false,
    val isOnline: Boolean = true,
    val pendingCount: Int = 0,
)

val BluetoothUiState.displayedDevices: List<BluetoothDevice>
    get() = if (showPendingOnly) {
        devices.filter { it.syncStatus != DeviceSyncStatus.SYNCED }
    } else {
        devices
    }

val BluetoothUiState.syncStatusMessage: String?
    get() = when {
        pendingCount == 0 -> null
        !isOnline -> "Offline — $pendingCount device(s) saved locally, will sync when internet returns"
        else -> "Online — syncing $pendingCount pending device(s) in background"
    }

/** Stable unique key for LazyColumn — Room auto-generated localId is always unique per row. */
fun deviceListKey(device: BluetoothDevice): String {
    return device.localId?.toString()
        ?: "${device.bleName}-${device.ssid}-${device.lastUpdatedAt}"
}

/** Used only for the short-lived NEW highlight after adding a device. */
fun deviceHighlightKey(device: BluetoothDevice): String = "${device.bleName}-${device.ssid}"
