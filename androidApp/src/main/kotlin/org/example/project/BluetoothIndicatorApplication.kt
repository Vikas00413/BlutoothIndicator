package org.example.project

import android.app.Application
import com.example.iotpoc.bluetooth.BluetoothLibrary
import org.example.project.di.androidPlatformDataModule
import org.example.project.di.dataModule
import org.example.project.di.initKoin
import org.example.project.di.useCaseModule
import org.example.project.di.viewModelModule
import org.example.project.sync.BluetoothSyncForegroundService
import org.example.project.sync.SyncNotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class BluetoothIndicatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@BluetoothIndicatorApplication)
            androidLogger(Level.DEBUG)
            modules(
                androidPlatformDataModule(this@BluetoothIndicatorApplication),
                dataModule,
                useCaseModule,
                viewModelModule,
            )
        }
        BluetoothLibrary.initialize(this)
        SyncNotificationHelper.createChannel(this)
        BluetoothSyncForegroundService.start(this)
    }
}
