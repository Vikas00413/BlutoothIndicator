package org.example.project.data.mapper

import org.example.project.data.local.entity.BluetoothDeviceEntity
import org.example.project.data.local.entity.SyncStatus
import org.example.project.data.model.request.BluetoothDeviceDto
import org.example.project.data.model.response.BluetoothListResponseItem
import org.example.project.domain.model.BluetoothDevice
import org.example.project.domain.model.DeviceSyncStatus
import org.example.project.domain.model.DomainConnectionStatus

fun BluetoothDeviceEntity.toDomain(): BluetoothDevice {
    return BluetoothDevice(
        id = serverId,
        localId = localId,
        ssid = ssid,
        bleName = bleName,
        lastSeenTime = lastSeenTime,
        connectionStatus = DomainConnectionStatus.valueOf(connectionStatus),
        createdAt = createdAt,
        lastUpdatedAt = lastUpdatedAt,
        syncStatus = syncStatus.toDeviceSyncStatus(),
    )
}

fun BluetoothListResponseItem.toEntity(
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    lastUpdatedAt: Long = lastSeenTime,
): BluetoothDeviceEntity {
    return BluetoothDeviceEntity(
        localId = 0,
        serverId = id,
        ssid = ssid,
        bleName = bleName,
        lastSeenTime = lastSeenTime,
        connectionStatus = connectionStatus.name,
        createdAt = createdAt,
        lastUpdatedAt = lastUpdatedAt,
        syncStatus = syncStatus.name,
    )
}

fun BluetoothDevice.toEntity(
    syncStatus: SyncStatus,
    lastUpdatedAt: Long,
): BluetoothDeviceEntity {
    return BluetoothDeviceEntity(
        localId = localId ?: 0,
        serverId = id,
        ssid = ssid,
        bleName = bleName,
        lastSeenTime = lastSeenTime,
        connectionStatus = connectionStatus.name,
        createdAt = createdAt,
        lastUpdatedAt = lastUpdatedAt,
        syncStatus = syncStatus.name,
    )
}

fun BluetoothDeviceEntity.toDto(): BluetoothDeviceDto {
    return BluetoothDeviceDto(
        ssid = ssid,
        bleName = bleName,
        lastSeenTime = lastSeenTime,
        connectionStatus = DomainConnectionStatus.valueOf(connectionStatus).toData(),
    )
}

private fun String.toDeviceSyncStatus(): DeviceSyncStatus {
    return runCatching { DeviceSyncStatus.valueOf(this) }
        .getOrDefault(DeviceSyncStatus.SYNCED)
}
