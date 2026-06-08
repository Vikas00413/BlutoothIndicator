package org.example.project.sync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.project.data.sync.BluetoothSyncManager
import org.example.project.platform.NetworkMonitor
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class BluetoothSyncForegroundService : Service(), KoinComponent {

    companion object {
        private const val TAG = "BluetoothSyncService"
        private const val SYNC_INTERVAL_MS = 30_000L

        fun start(context: Context) {
            val intent = Intent(context, BluetoothSyncForegroundService::class.java)
            context.startForegroundService(intent)
        }
    }

    private val syncManager: BluetoothSyncManager by inject()
    private val networkMonitor: NetworkMonitor by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private var periodicSyncJob: Job? = null

    private var isOnline = false
    private var pendingCount = 0
    private var isSyncing = false

    override fun onCreate() {
        super.onCreate()
        SyncNotificationHelper.createChannel(this)
        Log.d(TAG, "Foreground sync service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = SyncNotificationHelper.buildNotification(
            context = this,
            title = "Bluetooth sync active",
            message = "Monitoring pending devices",
        )
        startForeground(SyncNotificationHelper.NOTIFICATION_ID, notification)

        startMonitoring()
        startPeriodicSync()

        return START_STICKY
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        periodicSyncJob?.cancel()
        serviceScope.cancel()
        Log.d(TAG, "Foreground sync service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            combine(
                networkMonitor.observeOnline(),
                syncManager.observePendingCount(),
            ) { online, pending ->
                online to pending
            }.collect { (online, pending) ->
                val wasOffline = !isOnline && online
                isOnline = online
                pendingCount = pending
                updateNotification()

                if (wasOffline && pending > 0) {
                    Log.d(TAG, "Network restored — syncing $pending pending device(s)")
                    runSync()
                }
            }
        }
    }

    private fun startPeriodicSync() {
        periodicSyncJob?.cancel()
        periodicSyncJob = serviceScope.launch {
            while (isActive) {
                delay(SYNC_INTERVAL_MS)
                if (isOnline && pendingCount > 0) {
                    runSync()
                } else {
                    updateNotification()
                }
            }
        }
    }

    private suspend fun runSync() {
        if (!isOnline || pendingCount == 0 || isSyncing) return

        isSyncing = true
        updateNotification()

        val synced = syncManager.syncPendingRecords().getOrDefault(0)
        Log.d(TAG, "Sync finished — uploaded $synced device(s)")

        isSyncing = false
        updateNotification()
    }

    private fun updateNotification() {
        val title = if (pendingCount > 0) {
            SyncNotificationHelper.pendingTitle(pendingCount)
        } else {
            "Bluetooth sync active"
        }
        val message = SyncNotificationHelper.pendingMessage(isOnline, pendingCount)

        val notification = SyncNotificationHelper.buildNotification(
            context = this,
            title = title,
            message = message,
            isSyncing = isSyncing,
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(SyncNotificationHelper.NOTIFICATION_ID, notification)
    }
}
