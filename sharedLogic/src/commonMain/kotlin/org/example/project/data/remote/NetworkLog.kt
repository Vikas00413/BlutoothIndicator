package org.example.project.data.remote

internal expect fun networkLog(message: String)

internal expect fun networkLogError(message: String, throwable: Throwable? = null)
