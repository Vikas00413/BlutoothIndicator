package org.example.project.data.mapper

import org.example.project.data.model.request.BluetoothDeviceDto
import org.example.project.data.model.request.DataConnectionStatus
import org.example.project.data.model.response.BluetoothListResponseItem
import org.example.project.domain.model.BluetoothDevice
import org.example.project.domain.model.DomainConnectionStatus

fun BluetoothListResponseItem.toDomain(): BluetoothDevice {
    return BluetoothDevice(
        id = id,
        localId = null,
        ssid = ssid,
        bleName = bleName,
        lastSeenTime = lastSeenTime,
        connectionStatus = connectionStatus.toDomain(),
        createdAt = createdAt,
        lastUpdatedAt = lastSeenTime,
        syncStatus = org.example.project.domain.model.DeviceSyncStatus.SYNCED,
    )
}
fun DomainConnectionStatus.toData(): DataConnectionStatus {
    return when (this) {
        DomainConnectionStatus.CONNECTED -> DataConnectionStatus.CONNECTED
        DomainConnectionStatus.DISCONNECTED -> DataConnectionStatus.DISCONNECTED
        DomainConnectionStatus.CONNECTING -> DataConnectionStatus.CONNECTING
    }
}

fun DataConnectionStatus.toDomain(): DomainConnectionStatus {
    return when (this) {
        DataConnectionStatus.CONNECTED -> DomainConnectionStatus.CONNECTED
        DataConnectionStatus.DISCONNECTED -> DomainConnectionStatus.DISCONNECTED
        DataConnectionStatus.CONNECTING -> DomainConnectionStatus.CONNECTING
    }
}
fun BluetoothDevice.toDto(): BluetoothDeviceDto {
    return BluetoothDeviceDto(
        ssid = ssid,
        bleName = bleName,
        lastSeenTime = lastSeenTime,
        connectionStatus = connectionStatus.toData(),
    )
}