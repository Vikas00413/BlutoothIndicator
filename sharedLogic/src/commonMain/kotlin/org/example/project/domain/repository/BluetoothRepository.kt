package org.example.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.BluetoothDevice
import org.example.project.domain.model.DomainConnectionStatus

interface BluetoothRepository {
    fun observeDevices(): Flow<List<BluetoothDevice>>
    suspend fun refreshDevices(): Result<Unit>
    suspend fun saveDevice(
        ssid: String,
        bleName: String,
        lastSeenTime: Long,
        connectionStatus: DomainConnectionStatus,
    ): Result<String>
    suspend fun syncPendingDevices(): Result<Int>
}
