package org.example.project.di

import org.example.project.data.sync.BluetoothSyncManager
import org.koin.mp.KoinPlatform

fun initKoinIos() {
    initKoin {
        modules(iosPlatformDataModule(), dataModule, useCaseModule)
    }
    KoinPlatform.getKoin().get<BluetoothSyncManager>().startPeriodicSync()
}
