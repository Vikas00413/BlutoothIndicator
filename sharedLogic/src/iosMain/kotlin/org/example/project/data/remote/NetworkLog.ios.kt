package org.example.project.data.remote

internal actual fun networkLog(message: String) {
    println("[BluetoothAPI] $message")
}

internal actual fun networkLogError(message: String, throwable: Throwable?) {
    println("[BluetoothAPI][ERROR] $message")
    throwable?.printStackTrace()
}
