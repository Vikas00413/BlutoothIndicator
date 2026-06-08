package org.example.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.data.local.dao.BluetoothDeviceDao
import org.example.project.data.local.entity.SyncStatus
import org.example.project.data.mapper.toDomain
import org.example.project.data.mapper.toEntity
import org.example.project.data.sync.BluetoothSyncManager
import org.example.project.domain.model.BluetoothDevice
import org.example.project.domain.model.DomainConnectionStatus
import org.example.project.domain.repository.BluetoothRepository
import org.example.project.platform.NetworkMonitor

class BluetoothRepositoryImpl(
    private val dao: BluetoothDeviceDao,
    private val syncManager: BluetoothSyncManager,
    private val networkMonitor: NetworkMonitor,
) : BluetoothRepository {

    override fun observeDevices(): Flow<List<BluetoothDevice>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshDevices(): Result<Unit> = runCatching {
        val localDevices = dao.getAll()

        if (localDevices.isNotEmpty()) {
            // Offline-first: local data is returned via Flow immediately.
            // Refresh remote in background when online.
            if (networkMonitor.isOnline()) {
                syncManager.refreshFromRemote().getOrThrow()
            }
            return@runCatching
        }

        if (networkMonitor.isOnline()) {
            syncManager.refreshFromRemote().getOrThrow()
        } else {
            error("No local data and device is offline")
        }
    }

    override suspend fun saveDevice(
        ssid: String,
        bleName: String,
        lastSeenTime: Long,
        connectionStatus: DomainConnectionStatus,
    ): Result<String> = runCatching {
        val now = org.example.project.platform.currentTimeMillis()
        val entity = BluetoothDevice(
            id = null,
            localId = null,
            ssid = ssid,
            bleName = bleName,
            lastSeenTime = lastSeenTime,
            connectionStatus = connectionStatus,
            createdAt = now.toString(),
            lastUpdatedAt = now,
        ).toEntity(
            syncStatus = SyncStatus.PENDING_CREATE,
            lastUpdatedAt = now,
        )

        dao.insert(entity)

        if (networkMonitor.isOnline()) {
            syncManager.syncPendingRecords()
        }

        "Saved locally — will sync when online"
    }

    override suspend fun syncPendingDevices(): Result<Int> {
        return syncManager.syncPendingRecords()
    }
}
