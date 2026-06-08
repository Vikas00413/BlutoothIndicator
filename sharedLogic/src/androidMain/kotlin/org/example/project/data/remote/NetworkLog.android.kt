package org.example.project.data.remote

import android.util.Log

private const val TAG = "BluetoothAPI"
private const val MAX_LOG_LENGTH = 4000

internal actual fun networkLog(message: String) {
    logLongMessage(Log.DEBUG, message)
}

internal actual fun networkLogError(message: String, throwable: Throwable?) {
    logLongMessage(Log.ERROR, message)
    throwable?.let { Log.e(TAG, "Stacktrace:", it) }
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
