package org.example.project.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.model.DeviceSyncStatus
import org.example.project.domain.model.DomainConnectionStatus
import org.example.project.domain.usecase.ObserveBluetoothDevicesUseCase
import org.example.project.domain.usecase.RefreshBluetoothDevicesUseCase
import org.example.project.domain.usecase.SaveBluetoothDeviceUseCase
import org.example.project.platform.NetworkMonitor
import org.example.project.platform.currentTimeMillis

class BluetoothViewModel(
    private val observeDevicesUseCase: ObserveBluetoothDevicesUseCase,
    private val refreshDevicesUseCase: RefreshBluetoothDevicesUseCase,
    private val saveDeviceUseCase: SaveBluetoothDeviceUseCase,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    companion object {
        private const val TAG = "BluetoothVM"
        private const val HIGHLIGHT_DURATION_MS = 3_000L
        private val DEMO_DEVICE_NAME_REGEX = Regex("""DemoDevice(\d+)""")
    }

    private var deviceCounter = 0

    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    init {
        observeLocalDevices()
        observeNetworkStatus()
        refreshFromRemote()
    }

    private fun observeLocalDevices() {
        viewModelScope.launch {
            observeDevicesUseCase()
                .catch { error ->
                    Log.e(TAG, "observeDevices failed: ${error.message}", error)
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Failed to read local database")
                    }
                }
                .collect { devices ->
                    deviceCounter = maxOf(
                        deviceCounter,
                        devices.mapNotNull { device ->
                            DEMO_DEVICE_NAME_REGEX.find(device.bleName)?.groupValues?.get(1)?.toIntOrNull()
                        }.maxOrNull() ?: 0,
                    )
                    val pendingCount = devices.count { it.syncStatus != DeviceSyncStatus.SYNCED }
                    _uiState.update {
                        it.copy(
                            devices = devices,
                            pendingCount = pendingCount,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.observeOnline().collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
            }
        }
    }

    fun toggleShowPendingOnly() {
        _uiState.update { it.copy(showPendingOnly = !it.showPendingOnly) }
    }

    fun refreshFromRemote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, highlightedDeviceKey = null) }
            refreshDevicesUseCase()
                .onFailure { error ->
                    Log.e(TAG, "refreshFromRemote failed: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun addDevice() {
        viewModelScope.launch {
            deviceCounter++
            val deviceNumber = deviceCounter
            val lastSeenTime = currentTimeMillis()

            _uiState.update { it.copy(isSaving = true, saveMessage = null, errorMessage = null) }

            val bleName = "DemoDevice$deviceNumber"
            val ssid = "DemoWiFi$deviceNumber"
            Log.d(TAG, "addDevice start: $bleName / $ssid")

            saveDeviceUseCase(
                ssid = ssid,
                bleName = bleName,
                lastSeenTime = lastSeenTime,
                connectionStatus = demoStatusFor(deviceNumber),
            )
                .onSuccess { message ->
                    val key = "$bleName-$ssid"
                    Log.d(TAG, "addDevice success: $message")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            highlightedDeviceKey = key,
                            saveMessage = message,
                        )
                    }
                    clearHighlightAfterDelay(key)
                }
                .onFailure { error ->
                    deviceCounter--
                    Log.e(TAG, "addDevice failed: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Failed to add device",
                        )
                    }
                }
        }
    }

    private fun clearHighlightAfterDelay(key: String) {
        viewModelScope.launch {
            delay(HIGHLIGHT_DURATION_MS)
            _uiState.update { state ->
                if (state.highlightedDeviceKey == key) {
                    state.copy(highlightedDeviceKey = null)
                } else {
                    state
                }
            }
        }
    }

    private fun demoStatusFor(deviceNumber: Int): DomainConnectionStatus {
        return when (deviceNumber % 3) {
            0 -> DomainConnectionStatus.CONNECTED
            1 -> DomainConnectionStatus.CONNECTING
            else -> DomainConnectionStatus.DISCONNECTED
        }
    }
}
