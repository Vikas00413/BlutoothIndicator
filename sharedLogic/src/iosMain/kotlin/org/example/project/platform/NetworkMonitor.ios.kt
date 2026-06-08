package org.example.project.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class IosNetworkMonitor : NetworkMonitor {
    override fun isOnline(): Boolean = true

    override fun observeOnline(): Flow<Boolean> = flowOf(true)
}
