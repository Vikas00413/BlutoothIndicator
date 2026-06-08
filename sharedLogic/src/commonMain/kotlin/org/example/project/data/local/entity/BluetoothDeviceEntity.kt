package org.example.project.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bluetooth_devices")
data class BluetoothDeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val serverId: Int? = null,
    val ssid: String,
    val bleName: String,
    val lastSeenTime: Long,
    val connectionStatus: String,
    val createdAt: String?,
    val lastUpdatedAt: Long,
    val syncStatus: String,
)
