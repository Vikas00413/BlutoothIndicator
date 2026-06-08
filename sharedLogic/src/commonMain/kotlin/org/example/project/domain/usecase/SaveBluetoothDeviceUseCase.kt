package org.example.project.domain.usecase

import org.example.project.domain.model.DomainConnectionStatus
import org.example.project.domain.repository.BluetoothRepository

class SaveBluetoothDeviceUseCase(private val repository: BluetoothRepository) {
    suspend operator fun invoke(
        ssid: String,
        bleName: String,
        lastSeenTime: Long,
        connectionStatus: DomainConnectionStatus
    ): Result<String> {
        return repository.saveDevice(ssid, bleName, lastSeenTime, connectionStatus)
    }
}