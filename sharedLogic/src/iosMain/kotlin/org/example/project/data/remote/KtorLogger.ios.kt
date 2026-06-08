package org.example.project.data.remote

import io.ktor.client.plugins.logging.Logger

internal actual fun createKtorLogger(): Logger = object : Logger {
    override fun log(message: String) {
        println("[KtorClient] $message")
    }
}
