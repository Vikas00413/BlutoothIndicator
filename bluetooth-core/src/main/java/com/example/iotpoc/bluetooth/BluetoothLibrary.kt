package com.example.iotpoc.bluetooth

import android.content.Context
import android.content.Intent
import com.example.iotpoc.bluetooth.android.AndroidBluetoothController
import com.example.iotpoc.bluetooth.android.permission.AndroidBluetoothPermissionHandler
import com.example.iotpoc.bluetooth.android.service.BluetoothForegroundService
import com.example.iotpoc.bluetooth.android.storage.BluetoothDeviceDataStoreRepository
import com.example.iotpoc.bluetooth.domain.controller.BluetoothController
import com.example.iotpoc.bluetooth.domain.permission.BluetoothPermissionHandler
import com.example.iotpoc.bluetooth.domain.repository.BluetoothDeviceStorageRepository

/**
 * Single entry point for host apps.
 *
 * UI layer should depend only on [BluetoothController] flows and never touch Service/Receiver classes.
 */
object BluetoothLibrary {

    @Volatile
    private var controller: BluetoothController? = null

    @Volatile
    private var storageRepository: BluetoothDeviceStorageRepository? = null

    @Volatile
    private var permissionHandler: BluetoothPermissionHandler? = null

    fun initialize(context: Context): BluetoothController {
        val appContext = context.applicationContext

        val storage = BluetoothDeviceDataStoreRepository(appContext)
        val permissions = AndroidBluetoothPermissionHandler(appContext)
        val bluetoothController = AndroidBluetoothController(
            appContext = appContext,
            storageRepository = storage,
            permissionHandler = permissions,
        )

        storageRepository = storage
        permissionHandler = permissions
        controller = bluetoothController
        return bluetoothController
    }

    fun controllerOrNull(): BluetoothController? = controller

    fun requireController(): BluetoothController =
        controller ?: error("BluetoothLibrary.initialize(context) must be called first")

    fun permissionsOrNull(): BluetoothPermissionHandler? = permissionHandler

    fun storageOrNull(): BluetoothDeviceStorageRepository? = storageRepository

    /**
     * Optional helper for apps that want notification tap to open a specific Activity.
     */
    fun buildStartSessionIntent(context: Context, contentIntent: Intent): Intent {
        return Intent(context, BluetoothForegroundService::class.java).apply {
            action = BluetoothForegroundService.ACTION_START_SESSION
            putExtra(BluetoothForegroundService.EXTRA_CONTENT_INTENT, contentIntent)
        }
    }
}
