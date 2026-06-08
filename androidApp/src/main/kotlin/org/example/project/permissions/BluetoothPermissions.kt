package org.example.project.permissions

import android.Manifest
import android.os.Build
import com.example.iotpoc.bluetooth.BluetoothLibrary
import com.example.iotpoc.bluetooth.domain.permission.BluetoothPermissionHandler

object BluetoothPermissions {

    fun handlerOrNull(): BluetoothPermissionHandler? = BluetoothLibrary.permissionsOrNull()

    fun requiredRuntimePermissions(): Array<String> {
        val bluetooth = handlerOrNull()?.requiredPermissions().orEmpty()
        val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }
        return (bluetooth + notifications).distinct().toTypedArray()
    }

    fun hasAllRuntimePermissions(): Boolean {
        val handler = handlerOrNull() ?: return false
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handlerOrNull() != null // checked below via context in handler
            true // notification checked separately in MainActivity
        } else {
            true
        }
        return handler.hasAllPermissions() && notificationsGranted
    }

    fun hasBluetoothPermissions(): Boolean {
        return handlerOrNull()?.hasAllPermissions() == true
    }

    fun missingBluetoothPermissions(): List<String> {
        return handlerOrNull()?.missingPermissions().orEmpty()
    }
}
