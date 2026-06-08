package org.example.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json


expect fun createPlatformHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient


fun createHttpClient(): HttpClient {
    return createPlatformHttpClient {
        install(ContentNegotiation) {
            json(networkJson)
        }
        install(Logging) {
            logger = createKtorLogger()
            level = LogLevel.BODY
            sanitizeHeader { header -> header == "Authorization" }
        }
    }
}


