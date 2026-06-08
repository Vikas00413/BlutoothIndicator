package com.bluetooth.work.bluetoothdevice.dto

import com.bluetooth.work.bluetoothdevice.persistance.BluetoothEntity
import java.time.Instant

data class BluetoothItemResponse(
    val id: Long,
    val ssid: String,
    val bleName: String?,
    val lastSeenTime: Long,
    val connectionStatus: ConnectionStatus,
    val createdAt: Instant,
)

fun BluetoothEntity.toResponse() = BluetoothItemResponse(
    id = requireNotNull(id),
    ssid = ssid,
    bleName = bluetoothName,
    lastSeenTime = lastSeenTime,
    connectionStatus = connectionStatus,
    createdAt = createdAt,
)
