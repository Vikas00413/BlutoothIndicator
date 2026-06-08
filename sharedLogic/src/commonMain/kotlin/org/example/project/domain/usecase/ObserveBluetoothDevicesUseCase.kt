package org.example.project.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.BluetoothDevice
import org.example.project.domain.repository.BluetoothRepository

class ObserveBluetoothDevicesUseCase(
    private val repository: BluetoothRepository,
) {
    operator fun invoke(): Flow<List<BluetoothDevice>> = repository.observeDevices()
}
