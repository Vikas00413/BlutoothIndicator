package com.example.iotpoc.bluetooth.domain.permission

/**
 * Pure Kotlin permission contract.
 * Android implementation inspects runtime permissions; iOS/desktop targets can be added in KMP.
 */
interface BluetoothPermissionHandler {
    fun requiredPermissions(): List<String>

    fun hasAllPermissions(): Boolean

    fun missingPermissions(): List<String>
}
