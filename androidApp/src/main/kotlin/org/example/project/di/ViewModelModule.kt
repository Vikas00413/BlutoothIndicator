package org.example.project.di

import org.example.project.presentation.BluetoothViewModel
import org.example.project.presentation.ble.BluetoothBleViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::BluetoothViewModel)
    viewModelOf(::BluetoothBleViewModel)
}
