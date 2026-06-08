package org.example.project.di

import org.example.project.data.local.database.AppDatabase
import org.example.project.data.remote.BluetoothApi
import org.example.project.data.remote.createHttpClient
import org.example.project.data.repository.BluetoothRepositoryImpl
import org.example.project.data.sync.BluetoothSyncManager
import org.example.project.domain.repository.BluetoothRepository
import org.example.project.platform.getApiBaseUrl
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { createHttpClient() }
    single(named("baseUrl")) { getApiBaseUrl() }
    single { BluetoothApi(client = get(), baseUrl = get(named("baseUrl"))) }
    single { get<AppDatabase>().bluetoothDeviceDao() }
    singleOf(::BluetoothSyncManager)
    singleOf(::BluetoothRepositoryImpl) bind BluetoothRepository::class
}
