package org.example.project.data.model.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.project.data.model.request.DataConnectionStatus

@Serializable
data class BluetoothListResponseItem(
    @SerialName("bleName")
    val bleName: String,
    @SerialName("connectionStatus")
    val connectionStatus: DataConnectionStatus,
    @SerialName("createdAt")
    val createdAt: String,
    @SerialName("id")
    val id: Int,
    @SerialName("lastSeenTime")
    val lastSeenTime: Long,
    @SerialName("ssid")
    val ssid: String
)