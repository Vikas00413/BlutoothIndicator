package org.example.project.data.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BluetoothSaveResponse(
    @SerialName("message")
    var message: String,
) {

}