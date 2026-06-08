package org.example.project.di

import org.example.project.data.local.database.AppDatabase
import org.example.project.data.local.database.createRoomDatabase
import org.example.project.platform.IosNetworkMonitor
import org.example.project.platform.NetworkMonitor
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosPlatformDataModule(): Module = module {
    single<AppDatabase> { createRoomDatabase() }
    single<NetworkMonitor> { IosNetworkMonitor() }
}
