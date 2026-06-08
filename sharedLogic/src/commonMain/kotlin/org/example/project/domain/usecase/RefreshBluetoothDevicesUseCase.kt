package org.example.project.domain.usecase

import org.example.project.domain.repository.BluetoothRepository

class RefreshBluetoothDevicesUseCase(
    private val repository: BluetoothRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshDevices()
}
