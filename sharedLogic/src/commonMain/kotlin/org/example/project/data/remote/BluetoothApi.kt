package org.example.project.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.builtins.ListSerializer
import org.example.project.data.model.request.BluetoothDeviceDto
import org.example.project.data.model.response.BluetoothListResponseItem
import org.example.project.data.model.response.BluetoothSaveResponse

class BluetoothApi(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    init {
        networkLog("API base URL = $baseUrl")
    }

    suspend fun saveBluetoothData(data: BluetoothDeviceDto): BluetoothSaveResponse {
        val url = "$baseUrl/api/bluetooth/v1/save"
        val jsonBody = networkJson.encodeToString(BluetoothDeviceDto.serializer(), data)

        networkLog("→ POST $url")
        networkLog("→ Request body = $jsonBody")

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            val responseText = response.bodyAsText()
            logHttpResponse(url, response, responseText)
            networkJson.decodeFromString(BluetoothSaveResponse.serializer(), responseText)
        } catch (e: Exception) {
            networkLogError("✗ POST $url failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun getBluetoothDevices(): List<BluetoothListResponseItem> {
        val url = "$baseUrl/api/bluetooth/v1/list"
        networkLog("→ GET $url")

        return try {
            val response: HttpResponse = client.get(url)
            val responseText = response.bodyAsText()
            logHttpResponse(url, response, responseText)
            networkJson.decodeFromString(
                ListSerializer(BluetoothListResponseItem.serializer()),
                responseText,
            )
        } catch (e: Exception) {
            networkLogError("✗ GET $url failed: ${e.message}", e)
            throw e
        }
    }

    private fun logHttpResponse(url: String, response: HttpResponse, responseText: String) {
        networkLog(
            "← ${response.status.value} ${response.status.description} | URL: $url\n" +
                "Response body: $responseText",
        )
    }
}
