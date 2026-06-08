package org.example.project.di

import org.example.project.domain.usecase.ObserveBluetoothDevicesUseCase
import org.example.project.domain.usecase.RefreshBluetoothDevicesUseCase
import org.example.project.domain.usecase.SaveBluetoothDeviceUseCase
import org.example.project.domain.usecase.SyncBluetoothDevicesUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val useCaseModule = module {
    singleOf(::ObserveBluetoothDevicesUseCase)
    singleOf(::RefreshBluetoothDevicesUseCase)
    singleOf(::SaveBluetoothDeviceUseCase)
    singleOf(::SyncBluetoothDevicesUseCase)
}
