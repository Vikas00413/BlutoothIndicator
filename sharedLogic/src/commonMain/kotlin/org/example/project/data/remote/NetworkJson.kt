package org.example.project.data.remote

import kotlinx.serialization.json.Json

internal val networkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
