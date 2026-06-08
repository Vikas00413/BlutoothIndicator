package org.example.project.data.local.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun createRoomDatabase(): AppDatabase {
    val dbFilePath = documentDirectory() + "/bluetooth.db"
    return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun documentDirectory(): String {
    memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = errorPtr.ptr,
        )
        if (documentDirectory != null) {
            return requireNotNull(documentDirectory.path)
        }
        val error = errorPtr.value
        error("Couldn't determine document directory: ${error?.localizedDescription}")
    }
}
