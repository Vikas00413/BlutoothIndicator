package org.example.project.data.remote

import android.util.Log
import io.ktor.client.plugins.logging.Logger

private const val TAG = "KtorClient"
private const val MAX_LOG_LENGTH = 4000

internal actual fun createKtorLogger(): Logger = object : Logger {
    override fun log(message: String) {
        val priority = when {
            message.contains("EXCEPTION", ignoreCase = true) -> Log.ERROR
            message.contains("ERROR", ignoreCase = true) -> Log.ERROR
            message.startsWith("RESPONSE") && message.contains(" 4") -> Log.ERROR
            message.startsWith("RESPONSE") && message.contains(" 5") -> Log.ERROR
            else -> Log.DEBUG
        }
        logLongMessage(priority, message)
    }
}

private fun logLongMessage(priority: Int, message: String) {
    if (message.length <= MAX_LOG_LENGTH) {
        Log.println(priority, TAG, message)
        return
    }

    message.chunked(MAX_LOG_LENGTH).forEachIndexed { index, chunk ->
        Log.println(priority, TAG, "[$index] $chunk")
    }
}
