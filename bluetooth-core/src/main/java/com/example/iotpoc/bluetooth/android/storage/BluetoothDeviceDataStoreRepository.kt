package com.example.iotpoc.bluetooth.android.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.iotpoc.bluetooth.domain.model.SavedBluetoothDevice
import com.example.iotpoc.bluetooth.domain.repository.BluetoothDeviceStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.bluetoothDevicesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "bluetooth_core_saved_devices",
)

class BluetoothDeviceDataStoreRepository(
    context: Context,
) : BluetoothDeviceStorageRepository {

    private val dataStore = context.applicationContext.bluetoothDevicesDataStore

    override val savedDevices: Flow<List<SavedBluetoothDevice>> = dataStore.data.map { prefs ->
        val addresses = prefs[ADDRESSES_KEY].orEmpty()
            .split(SEPARATOR)
            .filter { it.isNotBlank() }

        addresses.map { address ->
            SavedBluetoothDevice(
                address = address,
                name = prefs[nameKey(address)],
                lastConnectedAtEpochMs = prefs[lastConnectedKey(address)] ?: 0L,
            )
        }.sortedByDescending { it.lastConnectedAtEpochMs }
    }

    override suspend fun saveDevice(device: SavedBluetoothDevice) {
        dataStore.edit { prefs ->
            val current = prefs[ADDRESSES_KEY].orEmpty()
                .split(SEPARATOR)
                .filter { it.isNotBlank() }
                .toMutableSet()
            current.add(device.address)
            prefs[ADDRESSES_KEY] = current.joinToString(SEPARATOR)
            device.name?.let { prefs[nameKey(device.address)] = it }
            prefs[lastConnectedKey(device.address)] = device.lastConnectedAtEpochMs
            prefs[LAST_CONNECTED_KEY] = device.address
        }
    }

    override suspend fun removeDevice(address: String) {
        dataStore.edit { prefs ->
            val current = prefs[ADDRESSES_KEY].orEmpty()
                .split(SEPARATOR)
                .filter { it.isNotBlank() && it != address }
            prefs[ADDRESSES_KEY] = current.joinToString(SEPARATOR)
            prefs.remove(nameKey(address))
            prefs.remove(lastConnectedKey(address))
            if (prefs[LAST_CONNECTED_KEY] == address) {
                prefs.remove(LAST_CONNECTED_KEY)
            }
        }
    }

    override suspend fun getLastConnectedAddress(): String? {
        return dataStore.data.first()[LAST_CONNECTED_KEY]
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    private fun nameKey(address: String) = stringPreferencesKey("name_$address")
    private fun lastConnectedKey(address: String) = longPreferencesKey("last_$address")

    companion object {
        private const val SEPARATOR = "|"
        private val ADDRESSES_KEY = stringPreferencesKey("saved_addresses")
        private val LAST_CONNECTED_KEY = stringPreferencesKey("last_connected_address")
    }
}
