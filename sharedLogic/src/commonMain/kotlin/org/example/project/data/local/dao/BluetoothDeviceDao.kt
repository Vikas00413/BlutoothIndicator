package org.example.project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.example.project.data.local.entity.BluetoothDeviceEntity
import org.example.project.data.local.entity.SyncStatus

@Dao
interface BluetoothDeviceDao {

    @Query("SELECT * FROM bluetooth_devices ORDER BY lastUpdatedAt DESC")
    fun observeAll(): Flow<List<BluetoothDeviceEntity>>

    @Query("SELECT * FROM bluetooth_devices ORDER BY lastUpdatedAt DESC")
    suspend fun getAll(): List<BluetoothDeviceEntity>

    @Query("SELECT * FROM bluetooth_devices WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): BluetoothDeviceEntity?

    @Query("SELECT * FROM bluetooth_devices WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Int): BluetoothDeviceEntity?

    @Query("SELECT * FROM bluetooth_devices WHERE bleName = :bleName AND ssid = :ssid LIMIT 1")
    suspend fun getByBleNameAndSsid(bleName: String, ssid: String): BluetoothDeviceEntity?

    @Query(
        """
        SELECT * FROM bluetooth_devices
        WHERE syncStatus IN (:pendingCreate, :pendingUpdate)
        ORDER BY lastUpdatedAt ASC
        """,
    )
    suspend fun getPendingSync(
        pendingCreate: String = SyncStatus.PENDING_CREATE.name,
        pendingUpdate: String = SyncStatus.PENDING_UPDATE.name,
    ): List<BluetoothDeviceEntity>

    @Query(
        """
        SELECT COUNT(*) FROM bluetooth_devices
        WHERE syncStatus IN (:pendingCreate, :pendingUpdate)
        """,
    )
    fun observePendingCount(
        pendingCreate: String = SyncStatus.PENDING_CREATE.name,
        pendingUpdate: String = SyncStatus.PENDING_UPDATE.name,
    ): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: BluetoothDeviceEntity): Long

    @Update
    suspend fun update(device: BluetoothDeviceEntity)

    @Query(
        """
        UPDATE bluetooth_devices
        SET serverId = :serverId,
            syncStatus = :syncedStatus,
            lastUpdatedAt = :lastUpdatedAt
        WHERE localId = :localId
        """,
    )
    suspend fun markSynced(
        localId: Long,
        serverId: Int?,
        lastUpdatedAt: Long,
        syncedStatus: String = SyncStatus.SYNCED.name,
    )

    @Query("DELETE FROM bluetooth_devices")
    suspend fun clearAll()
}
