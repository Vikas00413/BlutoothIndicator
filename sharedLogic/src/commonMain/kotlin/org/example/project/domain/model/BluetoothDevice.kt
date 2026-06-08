package org.example.project.domain.model

data class BluetoothDevice(
    val id: Int?,
    val localId: Long? = null,
    val ssid: String,
    val bleName: String,
    val lastSeenTime: Long,
    val connectionStatus: DomainConnectionStatus,
    val createdAt: String? = null,
    val lastUpdatedAt: Long = 0L,
    val syncStatus: DeviceSyncStatus = DeviceSyncStatus.SYNCED,
)
