package com.example.iotpoc.bluetooth.domain.model

/**
 * Pure Kotlin models — no Android imports.
 * These types are intended to move to commonMain when migrating to KMP.
 */
enum class BluetoothConnectionState {
    Idle,
    Connecting,
    Connected,
    Disconnected,
    Error,
}

data class SavedBluetoothDevice(
    val address: String,
    val name: String? = null,
    val lastConnectedAtEpochMs: Long = 0L,
)

data class DiscoveredBluetoothDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val lastSeenEpochMs: Long,
)

enum class BluetoothLogLevel {
    Debug,
    Info,
    Warning,
    Error,
}

data class BluetoothLogEntry(
    val timestampEpochMs: Long,
    val level: BluetoothLogLevel,
    val tag: String,
    val message: String,
)
