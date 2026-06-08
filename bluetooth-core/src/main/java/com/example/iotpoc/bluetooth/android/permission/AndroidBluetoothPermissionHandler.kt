package com.example.iotpoc.bluetooth.android.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.iotpoc.bluetooth.domain.permission.BluetoothPermissionHandler

class AndroidBluetoothPermissionHandler(
    private val context: Context,
) : BluetoothPermissionHandler {

    override fun requiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
    }

    override fun hasAllPermissions(): Boolean = missingPermissions().isEmpty()

    override fun missingPermissions(): List<String> {
        return requiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
}
