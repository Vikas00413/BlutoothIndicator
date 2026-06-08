package org.example.project.platform

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    fun isOnline(): Boolean
    fun observeOnline(): Flow<Boolean>
}
