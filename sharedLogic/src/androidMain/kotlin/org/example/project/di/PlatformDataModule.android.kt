package org.example.project.di

import android.content.Context
import org.example.project.data.local.database.AppDatabase
import org.example.project.data.local.database.createRoomDatabase
import org.example.project.platform.AndroidNetworkMonitor
import org.example.project.platform.NetworkMonitor
import org.koin.dsl.module

fun androidPlatformDataModule(context: Context) = module {
    single<AppDatabase> { createRoomDatabase(context.applicationContext) }
    single<NetworkMonitor> { AndroidNetworkMonitor(context.applicationContext) }
}
