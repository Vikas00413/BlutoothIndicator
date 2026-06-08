package org.example.project.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.project.data.local.dao.BluetoothDeviceDao
import org.example.project.data.local.entity.SyncStatus
import org.example.project.data.mapper.toDto
import org.example.project.data.mapper.toEntity
import org.example.project.data.remote.BluetoothApi
import org.example.project.data.remote.networkLog
import org.example.project.data.remote.networkLogError
import org.example.project.platform.NetworkMonitor

class BluetoothSyncManager(
    private val dao: BluetoothDeviceDao,
    private val api: BluetoothApi,
    private val networkMonitor: NetworkMonitor,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val syncMutex = Mutex()

    fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    /** Used on iOS where foreground service is not available. */
    fun startPeriodicSync(intervalMs: Long = 30_000L) {
        scope.launch {
            while (isActive) {
                syncPendingRecords()
                delay(intervalMs)
            }
        }
    }

    suspend fun syncPendingRecords(): Result<Int> = runCatching {
        if (!networkMonitor.isOnline()) return@runCatching 0

        syncMutex.withLock {
            val pending = dao.getPendingSync()
            var syncedCount = 0

            for (entity in pending) {
                try {
                    networkLog("Syncing pending device localId=${entity.localId}")
                    api.saveBluetoothData(entity.toDto())
                    dao.markSynced(
                        localId = entity.localId,
                        serverId = entity.serverId,
                        lastUpdatedAt = entity.lastUpdatedAt,
                    )
                    syncedCount++
                } catch (e: Exception) {
                    networkLogError("Sync failed for localId=${entity.localId}", e)
                }
            }
            syncedCount
        }
    }

    suspend fun refreshFromRemote(): Result<Unit> = runCatching {
        if (!networkMonitor.isOnline()) return@runCatching

        syncMutex.withLock {
            val remoteDevices = api.getBluetoothDevices()
            for (remote in remoteDevices) {
                val remoteUpdatedAt = remote.lastSeenTime
                val existing = dao.getByServerId(remote.id)
                    ?: dao.getByBleNameAndSsid(remote.bleName, remote.ssid)

                if (existing == null) {
                    dao.insert(remote.toEntity(lastUpdatedAt = remoteUpdatedAt))
                    continue
                }

                val localIsPending = existing.syncStatus != SyncStatus.SYNCED.name
                when {
                    localIsPending && existing.lastUpdatedAt > remoteUpdatedAt -> {
                        // Local wins — pending changes not yet on server
                    }
                    remoteUpdatedAt >= existing.lastUpdatedAt -> {
                        dao.insert(
                            remote.toEntity(
                                syncStatus = SyncStatus.SYNCED,
                                lastUpdatedAt = remoteUpdatedAt,
                            ).copy(
                                localId = existing.localId,
                                serverId = remote.id,
                            ),
                        )
                    }
                    else -> {
                        // Keep newer local record
                    }
                }
            }
        }
    }
}
