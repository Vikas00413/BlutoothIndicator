package org.example.project.domain.usecase

import org.example.project.domain.repository.BluetoothRepository

class SyncBluetoothDevicesUseCase(
    private val repository: BluetoothRepository,
) {
    suspend operator fun invoke(): Result<Int> = repository.syncPendingDevices()
}
