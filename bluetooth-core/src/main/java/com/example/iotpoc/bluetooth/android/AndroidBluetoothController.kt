package com.example.iotpoc.bluetooth.android

import android.content.Context
import android.content.Intent
import com.example.iotpoc.bluetooth.android.internal.BluetoothSessionStore
import com.example.iotpoc.bluetooth.android.permission.AndroidBluetoothPermissionHandler
import com.example.iotpoc.bluetooth.android.service.BluetoothForegroundService
import com.example.iotpoc.bluetooth.android.storage.BluetoothDeviceDataStoreRepository
import com.example.iotpoc.bluetooth.domain.controller.BluetoothController
import com.example.iotpoc.bluetooth.domain.model.BluetoothConnectionState
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogEntry
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogLevel
import com.example.iotpoc.bluetooth.domain.model.DiscoveredBluetoothDevice
import com.example.iotpoc.bluetooth.domain.model.SavedBluetoothDevice
import com.example.iotpoc.bluetooth.domain.permission.BluetoothPermissionHandler
import com.example.iotpoc.bluetooth.domain.repository.BluetoothDeviceStorageRepository
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

internal class AndroidBluetoothController(
    private val appContext: Context,
    private val storageRepository: BluetoothDeviceStorageRepository,
    val permissionHandler: BluetoothPermissionHandler,
) : BluetoothController {

    override val connectionState: StateFlow<BluetoothConnectionState> =
        BluetoothSessionStore.connectionState

    override val connectedAddress: StateFlow<String?> =
        BluetoothSessionStore.connectedAddress

    override val savedDevices: StateFlow<List<SavedBluetoothDevice>> =
        BluetoothSessionStore.savedDevices

    override val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> =
        BluetoothSessionStore.discoveredDevices

    override val logs: SharedFlow<BluetoothLogEntry> =
        BluetoothSessionStore.logs

    override val isScanning: StateFlow<Boolean> =
        BluetoothSessionStore.isScanning

    override val isGattReady: StateFlow<Boolean> =
        BluetoothSessionStore.isGattReady

    override val receivedMessages: StateFlow<List<String>> =
        BluetoothSessionStore.receivedMessages

    override val lastError: StateFlow<String?> =
        BluetoothSessionStore.lastError

    override suspend fun startForegroundSession() {
        ensurePermissionsOrThrow()
        BluetoothForegroundService.startSession(appContext)
    }

    override suspend fun stopForegroundSession() {
        BluetoothForegroundService.stopSession(appContext)
    }

    override suspend fun startScan() {
        ensurePermissionsOrThrow()
        BluetoothForegroundService.sendAction(
            appContext,
            BluetoothForegroundService.ACTION_START_SCAN,
        )
    }

    override suspend fun stopScan() {
        BluetoothForegroundService.sendAction(
            appContext,
            BluetoothForegroundService.ACTION_STOP_SCAN,
        )
    }

    override suspend fun connect(address: String, displayName: String?) {
        ensurePermissionsOrThrow()
        BluetoothForegroundService.sendAction(
            appContext,
            BluetoothForegroundService.ACTION_CONNECT,
        ) {
            putExtra(BluetoothForegroundService.EXTRA_ADDRESS, address)
            putExtra(BluetoothForegroundService.EXTRA_NAME, displayName)
        }
    }

    override suspend fun disconnect() {
        BluetoothForegroundService.sendAction(
            appContext,
            BluetoothForegroundService.ACTION_DISCONNECT,
        )
    }

    override suspend fun reconnectLastDevice() {
        ensurePermissionsOrThrow()
        BluetoothForegroundService.sendAction(
            appContext,
            BluetoothForegroundService.ACTION_RECONNECT_LAST,
        )
    }

    override suspend fun sendText(text: String) {
        ensurePermissionsOrThrow()
        BluetoothForegroundService.sendAction(
            appContext,
            BluetoothForegroundService.ACTION_SEND_TEXT,
        ) {
            putExtra(BluetoothForegroundService.EXTRA_TEXT, text)
        }
    }

    override fun clearDiscoveredDevices() {
        BluetoothSessionStore.clearDiscovered()
        BluetoothSessionStore.setLastError(null)
    }

    override fun clearMessages() {
        BluetoothSessionStore.clearMessages()
    }

    private fun ensurePermissionsOrThrow() {
        if (!permissionHandler.hasAllPermissions()) {
            val missing = permissionHandler.missingPermissions().joinToString()
            BluetoothSessionStore.log(
                BluetoothLogLevel.Error,
                "Permissions",
                "Missing permissions: $missing",
            )
            throw SecurityException("Missing Bluetooth permissions: $missing")
        }
    }
}
