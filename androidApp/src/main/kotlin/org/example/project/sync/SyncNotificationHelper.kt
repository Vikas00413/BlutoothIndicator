package org.example.project.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.example.project.MainActivity

object SyncNotificationHelper {

    const val CHANNEL_ID = "bluetooth_sync_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bluetooth Sync",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows pending sync status and keeps sync running in background"
            setShowBadge(false)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context,
        title: String,
        message: String,
        isSyncing: Boolean = false,
    ): Notification {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .apply {
                if (isSyncing) {
                    setProgress(0, 0, true)
                }
            }
            .build()
    }

    fun pendingTitle(pendingCount: Int): String {
        return if (pendingCount == 1) {
            "1 device pending sync"
        } else {
            "$pendingCount devices pending sync"
        }
    }

    fun pendingMessage(isOnline: Boolean, pendingCount: Int): String {
        return when {
            pendingCount == 0 -> "All devices synced with server"
            isOnline -> "Syncing pending devices with server..."
            else -> "Offline — will sync automatically when internet is available"
        }
    }
}
