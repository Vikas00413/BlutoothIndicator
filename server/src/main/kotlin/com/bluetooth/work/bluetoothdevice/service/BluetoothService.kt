package com.bluetooth.work.bluetoothdevice.service

import com.bluetooth.work.bluetoothdevice.dto.BluetoothItemResponse
import com.bluetooth.work.bluetoothdevice.dto.BluetoothRequest
import com.bluetooth.work.bluetoothdevice.dto.toResponse
import com.bluetooth.work.bluetoothdevice.persistance.BluetoothEntity
import com.bluetooth.work.bluetoothdevice.persistance.BluetoothRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class BluetoothService(
    val bluetoothRepository: BluetoothRepository
) {
    fun saveBlueTooth(request: BluetoothRequest) {

        bluetoothRepository.save(
            BluetoothEntity(
                ssid = request.ssid,
                bluetoothName = request.bleName,
                lastSeenTime = request.lastSeenTime,
                connectionStatus = request.connectionStatus
            )
        )

    }

    fun getAllBluetooth(): List<BluetoothItemResponse> {
        return bluetoothRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
            .map { it.toResponse() }
    }
}