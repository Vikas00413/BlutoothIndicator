package com.example.iotpoc.bluetooth.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.iotpoc.bluetooth.android.internal.BluetoothSessionStore
import com.example.iotpoc.bluetooth.android.permission.AndroidBluetoothPermissionHandler
import com.example.iotpoc.bluetooth.android.service.BluetoothForegroundService
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogLevel

/**
 * Restarts the Bluetooth Foreground Service after device reboot and attempts reconnection
 * to the last saved device MAC address.
 */
class BluetoothBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val permissionHandler = AndroidBluetoothPermissionHandler(context.applicationContext)
        if (!permissionHandler.hasAllPermissions()) {
            BluetoothSessionStore.log(
                BluetoothLogLevel.Warning,
                "BootReceiver",
                "Skipping BLE restart — Bluetooth permissions not granted",
            )
            return
        }

        BluetoothSessionStore.log(
            BluetoothLogLevel.Info,
            "BootReceiver",
            "BOOT_COMPLETED received — restarting Bluetooth session",
        )

        BluetoothForegroundService.startSession(context.applicationContext)
        BluetoothForegroundService.sendAction(
            context.applicationContext,
            BluetoothForegroundService.ACTION_RECONNECT_LAST,
        )
    }
}
