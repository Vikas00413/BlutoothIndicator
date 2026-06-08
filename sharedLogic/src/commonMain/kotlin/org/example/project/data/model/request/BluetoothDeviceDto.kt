package org.example.project.data.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DataConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
}
@Serializable
data class BluetoothDeviceDto(
    @SerialName("bleName")
    val bleName: String,
    @SerialName("connectionStatus")
    val connectionStatus: DataConnectionStatus,
    @SerialName("lastSeenTime")
    val lastSeenTime: Long,
    @SerialName("ssid")
    val ssid: String
)