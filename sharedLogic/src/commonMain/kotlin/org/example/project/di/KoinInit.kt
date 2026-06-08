package org.example.project.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    if (KoinPlatform.getKoinOrNull() != null) return

    startKoin {
        appDeclaration()
    }
}
