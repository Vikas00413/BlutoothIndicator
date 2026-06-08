package com.example.iotpoc.bluetooth.android.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import com.example.iotpoc.bluetooth.android.internal.BluetoothSessionStore
import com.example.iotpoc.bluetooth.domain.model.BluetoothConnectionState
import com.example.iotpoc.bluetooth.domain.model.BluetoothLogLevel
import com.example.iotpoc.bluetooth.domain.model.DiscoveredBluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.util.UUID

@SuppressLint("MissingPermission")
internal class AndroidBleEngine(
    private val context: Context,
    private val scope: CoroutineScope,
) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private var scanJob: Job? = null
    private var scanCallback: ScanCallback? = null
    private var gatt: BluetoothGatt? = null
    private var connectedAddress: String? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

    private val serviceUuid = UUID.fromString("00001111-0000-1000-8000-00805f9b34fb")
    private val simpleCharUuid = UUID.fromString("00002222-0000-1000-8000-00805f9b34fb")
    private val deadServiceUuid = UUID.fromString("0000dead-0000-1000-8000-00805f9b34fb")
    private val beefCharUuid = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")
    private val tempAlarmServiceUuid = UUID.fromString("DEADF154-0000-0000-0000-0000DEADF154")
    private val tempAlarmRxUuid = UUID.fromString("EDEDEDED-DEAD-F154-1319-740381000000")
    private val tempAlarmTxUuid = UUID.fromString("CCCCFFFF-DEAD-F154-1319-740381000000")
    private val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    fun startScan(durationMs: Long = 12_000) {
        if (scanJob?.isActive == true) return

        val btAdapter = adapter ?: run {
            fail("Bluetooth adapter unavailable")
            return
        }
        if (!btAdapter.isEnabled) {
            fail("Bluetooth is disabled")
            return
        }

        val scanner = btAdapter.bluetoothLeScanner ?: run {
            fail("BLE scanner unavailable")
            return
        }

        BluetoothSessionStore.clearDiscovered()
        BluetoothSessionStore.setScanning(true)
        log(BluetoothLogLevel.Info, "Starting BLE scan")

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val name = device.name ?: result.scanRecord?.deviceName ?: "Unknown"
                BluetoothSessionStore.upsertDiscovered(
                    DiscoveredBluetoothDevice(
                        address = device.address,
                        name = name,
                        rssi = result.rssi,
                        lastSeenEpochMs = System.currentTimeMillis(),
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                fail("Scan failed: $errorCode")
                BluetoothSessionStore.setScanning(false)
            }
        }
        scanCallback = callback

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(emptyList(), settings, callback)
        scanJob = scope.launch {
            delay(durationMs)
            stopScanInternal(scanner, callback)
            log(BluetoothLogLevel.Info, "BLE scan stopped")
        }
    }

    fun stopScan() {
        val btAdapter = adapter ?: return
        val scanner = btAdapter.bluetoothLeScanner ?: return
        scanCallback?.let { stopScanInternal(scanner, it) }
    }

    private fun stopScanInternal(
        scanner: android.bluetooth.le.BluetoothLeScanner,
        callback: ScanCallback,
    ) {
        scanJob?.cancel()
        scanJob = null
        runCatching { scanner.stopScan(callback) }
        scanCallback = null
        BluetoothSessionStore.setScanning(false)
    }

    fun connect(address: String) {
        val btAdapter = adapter ?: run {
            fail("Bluetooth adapter unavailable")
            return
        }
        val device = runCatching { btAdapter.getRemoteDevice(address) }.getOrNull() ?: run {
            fail("Invalid device address: $address")
            return
        }

        disconnect()
        BluetoothSessionStore.setGattReady(false)
        BluetoothSessionStore.clearMessages()
        BluetoothSessionStore.setConnectionState(BluetoothConnectionState.Connecting)
        BluetoothSessionStore.setConnectedAddress(address)
        log(BluetoothLogLevel.Info, "Connecting to $address")

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    fail("GATT error status=$status")
                    runCatching { gatt.close() }
                    return
                }

                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        connectedAddress = address
                        BluetoothSessionStore.setConnectionState(BluetoothConnectionState.Connected)
                        log(BluetoothLogLevel.Info, "Connected to $address")
                        runCatching { gatt.discoverServices() }
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        connectedAddress = null
                        rxCharacteristic = null
                        txCharacteristic = null
                        BluetoothSessionStore.setGattReady(false)
                        BluetoothSessionStore.setConnectionState(BluetoothConnectionState.Disconnected)
                        BluetoothSessionStore.setConnectedAddress(null)
                        log(BluetoothLogLevel.Warning, "Disconnected from $address")
                        runCatching { gatt.close() }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    fail("Service discovery failed: $status")
                    return
                }

                log(BluetoothLogLevel.Info, "Services discovered for $address")
                gatt.services.orEmpty().forEach { service ->
                    log(BluetoothLogLevel.Debug, "Service uuid=${service.uuid}")
                    service.characteristics.orEmpty().forEach { characteristic ->
                        log(
                            BluetoothLogLevel.Debug,
                            "  Char uuid=${characteristic.uuid} props=${characteristic.properties}",
                        )
                    }
                }

                val service = gatt.getService(tempAlarmServiceUuid)
                    ?: gatt.getService(serviceUuid)
                    ?: gatt.getService(deadServiceUuid)
                    ?: gatt.services.firstOrNull()

                rxCharacteristic = service?.getCharacteristic(tempAlarmRxUuid)
                    ?: service?.getCharacteristic(simpleCharUuid)
                    ?: service?.getCharacteristic(beefCharUuid)
                    ?: service?.characteristics?.firstOrNull()

                txCharacteristic = service?.getCharacteristic(tempAlarmTxUuid)
                    ?: service?.getCharacteristic(simpleCharUuid)
                    ?: service?.getCharacteristic(beefCharUuid)
                    ?: service?.characteristics?.firstOrNull()

                val tx = txCharacteristic
                if (service == null || rxCharacteristic == null || tx == null) {
                    fail("GATT profile not found. Check LightBlue service/characteristics.")
                    return
                }

                gatt.setCharacteristicNotification(tx, true)
                tx.getDescriptor(cccdUuid)?.let { cccd ->
                    val value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    val ok = if (Build.VERSION.SDK_INT >= 33) {
                        gatt.writeDescriptor(cccd, value) == BluetoothGatt.GATT_SUCCESS
                    } else {
                        @Suppress("DEPRECATION")
                        runCatching {
                            cccd.value = value
                            gatt.writeDescriptor(cccd)
                        }.getOrDefault(false)
                    }
                    log(BluetoothLogLevel.Info, "CCCD write started=$ok for tx=${tx.uuid}")
                }

                runCatching { gatt.readCharacteristic(tx) }
                BluetoothSessionStore.setGattReady(true)
                log(BluetoothLogLevel.Info, "GATT ready")
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int,
            ) {
                if (status != BluetoothGatt.GATT_SUCCESS) return
                if (characteristic.uuid == txCharacteristic?.uuid) {
                    emitText(value, "RX(read)")
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
            ) {
                if (characteristic.uuid == txCharacteristic?.uuid) {
                    emitText(value, "RX(notify)")
                }
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt,
                descriptor: BluetoothGattDescriptor,
                status: Int,
            ) {
                log(BluetoothLogLevel.Debug, "CCCD write done status=$status")
            }
        }

        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
        } else {
            @Suppress("DEPRECATION")
            device.connectGatt(context, false, callback)
        }
    }

    fun disconnect() {
        runCatching { gatt?.disconnect() }
        runCatching { gatt?.close() }
        gatt = null
        connectedAddress = null
        rxCharacteristic = null
        txCharacteristic = null
        BluetoothSessionStore.setGattReady(false)
        BluetoothSessionStore.setConnectedAddress(null)
        BluetoothSessionStore.setConnectionState(BluetoothConnectionState.Disconnected)
    }

    fun writeText(text: String): Boolean {
        val activeGatt = gatt ?: return false
        val rx = rxCharacteristic ?: return false
        val bytes = text.toByteArray(Charsets.UTF_8)
        log(BluetoothLogLevel.Info, "TX(write): $text")

        return if (Build.VERSION.SDK_INT >= 33) {
            activeGatt.writeCharacteristic(rx, bytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) ==
                BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            runCatching {
                rx.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                rx.value = bytes
                activeGatt.writeCharacteristic(rx)
            }.getOrDefault(false)
        }
    }

    private fun emitText(value: ByteArray, source: String) {
        val text = value.toString(Charset.forName("UTF-8")).trim()
        if (text.isBlank()) return
        log(BluetoothLogLevel.Info, "$source: $text")
        BluetoothSessionStore.appendReceivedMessage(text)
    }

    private fun fail(message: String) {
        BluetoothSessionStore.setConnectionState(BluetoothConnectionState.Error)
        BluetoothSessionStore.setLastError(message)
        log(BluetoothLogLevel.Error, message)
    }

    private fun log(level: BluetoothLogLevel, message: String) {
        BluetoothSessionStore.log(level, "BleEngine", message)
    }
}
