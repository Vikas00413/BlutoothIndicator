package com.example.iotpoc.bluetooth.android.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.iotpoc.bluetooth.R
import com.example.iotpoc.bluetooth.domain.model.BluetoothConnectionState

internal object BluetoothNotificationHelper {
    const val CHANNEL_ID = "bluetooth_core_connection"
    const val NOTIFICATION_ID = 4101

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bluetooth Connection",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows Bluetooth device connection status"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        state: BluetoothConnectionState,
        connectedAddress: String?,
        deviceLabel: String?,
        contentIntent: Intent?,
    ): Notification {
        val label = deviceLabel ?: connectedAddress

        val title = when (state) {
            BluetoothConnectionState.Connected -> "Connected"
            BluetoothConnectionState.Connecting -> "Connecting..."
            BluetoothConnectionState.Disconnected -> "Disconnected"
            BluetoothConnectionState.Error -> "Connection error"
            BluetoothConnectionState.Idle -> "Bluetooth ready"
        }

        val body = when (state) {
            BluetoothConnectionState.Connected ->
                label?.let { "Device: $it" } ?: "Device connected"
            BluetoothConnectionState.Connecting ->
                label?.let { "Connecting to $it" } ?: "Connecting to device..."
            BluetoothConnectionState.Disconnected ->
                label?.let { "Disconnected from $it" } ?: "No device connected"
            BluetoothConnectionState.Error ->
                label?.let { "Error with $it" } ?: "Bluetooth connection failed"
            BluetoothConnectionState.Idle ->
                "Tap app to scan nearby devices"
        }

        val pendingIntent = contentIntent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_bluetooth_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
