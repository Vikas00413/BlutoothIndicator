package org.example.project.data.local.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.example.project.data.local.dao.BluetoothDeviceDao
import org.example.project.data.local.entity.BluetoothDeviceEntity

@Database(
    entities = [BluetoothDeviceEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bluetoothDeviceDao(): BluetoothDeviceDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
