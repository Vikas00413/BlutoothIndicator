package com.example.iotpoc.bluetooth.android.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.iotpoc.bluetooth.android.permission.AndroidBluetoothPermissionHandler
import com.example.iotpoc.bluetooth.android.ble.AndroidBleEngine
import com.example.iotpoc.bluetooth.android.internal.BluetoothSessionStore
import com.example.iotpoc.bluetooth.android.notification.BluetoothNotificationHelper
import com.example.iotpoc.bluetooth.android.storage.BluetoothDeviceDataStoreRepository
import com.example.iotpoc.bluetooth.domain.model.BluetoothConnectionState
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogLevel
import com.example.iotpoc.bluetooth.domain.model.SavedBluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BluetoothForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var bleEngine: AndroidBleEngine
    private lateinit var storageRepository: BluetoothDeviceDataStoreRepository
    private var isInForeground = false

    override fun onCreate() {
        super.onCreate()
        bleEngine = AndroidBleEngine(this, serviceScope)
        storageRepository = BluetoothDeviceDataStoreRepository(this)
        BluetoothNotificationHelper.ensureChannel(this)

        serviceScope.launch {
            storageRepository.savedDevices.collectLatest { devices ->
                BluetoothSessionStore.setSavedDevices(devices)
            }
        }

        serviceScope.launch {
            combine(
                BluetoothSessionStore.connectionState,
                BluetoothSessionStore.connectedAddress,
                BluetoothSessionStore.lastDeviceLabel,
            ) { state, address, label ->
                Triple(state, address, label)
            }.collectLatest { (state, address, label) ->
                updateNotification(state, address, label)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SESSION -> stopSession()
            else -> {
                // Android requires startForeground() soon after startForegroundService().
                // Scan/connect intents can arrive before ACTION_START_SESSION, so always promote first.
                ensureInForeground(intent)
                when (intent?.action) {
                    ACTION_START_SESSION -> {
                        BluetoothSessionStore.log(BluetoothLogLevel.Info, "Service", "Foreground session started")
                    }
                    ACTION_START_SCAN -> bleEngine.startScan()
                    ACTION_STOP_SCAN -> bleEngine.stopScan()
                    ACTION_CONNECT -> {
                        val address = intent.getStringExtra(EXTRA_ADDRESS).orEmpty()
                        val name = intent.getStringExtra(EXTRA_NAME)
                        connect(address, name)
                    }
                    ACTION_DISCONNECT -> bleEngine.disconnect()
                    ACTION_SEND_TEXT -> {
                        val text = intent.getStringExtra(EXTRA_TEXT).orEmpty()
                        if (text.isNotBlank()) {
                            val ok = bleEngine.writeText(text)
                            if (ok) {
                                BluetoothSessionStore.appendReceivedMessage("You: $text")
                            } else {
                                BluetoothSessionStore.setLastError(
                                    "Send failed (not connected or characteristic missing)",
                                )
                            }
                        }
                    }
                    ACTION_RECONNECT_LAST -> serviceScope.launch { reconnectLast() }
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        bleEngine.disconnect()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun ensureInForeground(intent: Intent?) {
        if (isInForeground) return

        val permissionHandler = AndroidBluetoothPermissionHandler(applicationContext)
        if (!permissionHandler.hasAllPermissions()) {
            val missing = permissionHandler.missingPermissions().joinToString()
            Log.e(TAG, "Cannot start connectedDevice FGS — missing permissions: $missing")
            BluetoothSessionStore.setLastError(
                "Bluetooth permissions required before starting BLE service: $missing",
            )
            stopSelf()
            return
        }

        val contentIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_CONTENT_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_CONTENT_INTENT)
        }
        val notification = BluetoothNotificationHelper.buildNotification(
            context = this,
            state = BluetoothSessionStore.connectionState.value,
            connectedAddress = BluetoothSessionStore.connectedAddress.value,
            deviceLabel = BluetoothSessionStore.lastDeviceLabel.value,
            contentIntent = contentIntent,
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    BluetoothNotificationHelper.NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
                )
            } else {
                @Suppress("DEPRECATION")
                startForeground(BluetoothNotificationHelper.NOTIFICATION_ID, notification)
            }
            isInForeground = true
        } catch (securityException: SecurityException) {
            Log.e(TAG, "startForeground failed — grant Bluetooth permissions first", securityException)
            BluetoothSessionStore.setLastError(
                "Bluetooth permissions required. Open BLE Scan tab and grant permissions.",
            )
            stopSelf()
        }
    }

    private fun stopSession() {
        bleEngine.stopScan()
        bleEngine.disconnect()
        isInForeground = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        BluetoothSessionStore.log(BluetoothLogLevel.Info, "Service", "Foreground session stopped")
    }

    private fun connect(address: String, name: String?) {
        if (address.isBlank()) return
        BluetoothSessionStore.setLastDeviceLabel(name?.takeIf { it.isNotBlank() } ?: address)
        bleEngine.connect(address)
        serviceScope.launch {
            storageRepository.saveDevice(
                SavedBluetoothDevice(
                    address = address,
                    name = name,
                    lastConnectedAtEpochMs = System.currentTimeMillis(),
                )
            )
        }
    }

    private suspend fun reconnectLast() {
        val lastAddress = storageRepository.getLastConnectedAddress() ?: return
        val saved = storageRepository.savedDevices.first()
            .firstOrNull { it.address == lastAddress }
        connect(lastAddress, saved?.name)
    }

    private fun updateNotification(
        state: BluetoothConnectionState,
        connectedAddress: String?,
        deviceLabel: String?,
    ) {
        if (!isInForeground) return

        val notification = BluetoothNotificationHelper.buildNotification(
            context = this,
            state = state,
            connectedAddress = connectedAddress,
            deviceLabel = deviceLabel,
            contentIntent = null,
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(BluetoothNotificationHelper.NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "BluetoothFgService"

        const val ACTION_START_SESSION = "com.example.iotpoc.bluetooth.action.START_SESSION"
        const val ACTION_STOP_SESSION = "com.example.iotpoc.bluetooth.action.STOP_SESSION"
        const val ACTION_START_SCAN = "com.example.iotpoc.bluetooth.action.START_SCAN"
        const val ACTION_STOP_SCAN = "com.example.iotpoc.bluetooth.action.STOP_SCAN"
        const val ACTION_CONNECT = "com.example.iotpoc.bluetooth.action.CONNECT"
        const val ACTION_DISCONNECT = "com.example.iotpoc.bluetooth.action.DISCONNECT"
        const val ACTION_RECONNECT_LAST = "com.example.iotpoc.bluetooth.action.RECONNECT_LAST"
        const val ACTION_SEND_TEXT = "com.example.iotpoc.bluetooth.action.SEND_TEXT"

        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_CONTENT_INTENT = "extra_content_intent"

        fun startSession(context: Context, contentIntent: Intent? = null) {
            if (!AndroidBluetoothPermissionHandler(context).hasAllPermissions()) {
                Log.w(TAG, "startSession skipped — Bluetooth permissions not granted")
                BluetoothSessionStore.setLastError(
                    "Grant Bluetooth permissions before starting BLE session",
                )
                return
            }
            val intent = Intent(context, BluetoothForegroundService::class.java).apply {
                action = ACTION_START_SESSION
                contentIntent?.let { putExtra(EXTRA_CONTENT_INTENT, it) }
            }
            context.startForegroundService(intent)
        }

        fun stopSession(context: Context) {
            val intent = Intent(context, BluetoothForegroundService::class.java).apply {
                action = ACTION_STOP_SESSION
            }
            context.startService(intent)
        }

        fun sendAction(context: Context, action: String, configure: Intent.() -> Unit = {}) {
            if (!AndroidBluetoothPermissionHandler(context).hasAllPermissions()) {
                Log.w(TAG, "sendAction($action) skipped — Bluetooth permissions not granted")
                BluetoothSessionStore.setLastError(
                    "Grant Bluetooth permissions before using BLE features",
                )
                return
            }
            val intent = Intent(context, BluetoothForegroundService::class.java).apply {
                this.action = action
                configure()
            }
            context.startForegroundService(intent)
        }
    }
}
