package com.bluetooth.work.bluetoothdevice.dto

data class BluetoothRequest(val ssid: String,val bleName:String?, val lastSeenTime: Long,var connectionStatus: ConnectionStatus)

enum class ConnectionStatus{
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
}