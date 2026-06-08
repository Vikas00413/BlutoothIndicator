package org.example.project.data.remote

import io.ktor.client.plugins.logging.Logger

internal expect fun createKtorLogger(): Logger
