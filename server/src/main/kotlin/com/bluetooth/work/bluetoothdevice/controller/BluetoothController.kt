package com.bluetooth.work.bluetoothdevice.controller

import com.bluetooth.work.bluetoothdevice.dto.BlueToothResponse
import com.bluetooth.work.bluetoothdevice.dto.BluetoothItemResponse
import com.bluetooth.work.bluetoothdevice.dto.BluetoothRequest
import com.bluetooth.work.bluetoothdevice.service.BluetoothService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bluetooth")
class BluetoothController(
    val bluetoothService: BluetoothService
) {

    @PostMapping("/v1/save")
    fun saveBlueTooth(@RequestBody request: BluetoothRequest): ResponseEntity<BlueToothResponse> {
        bluetoothService.saveBlueTooth(request)
        return ResponseEntity.ok(BlueToothResponse("Save Successfully"))
    }

    @GetMapping("/v1/list")
    fun getAllBluetooth(): ResponseEntity<List<BluetoothItemResponse>> {
        return ResponseEntity.ok(bluetoothService.getAllBluetooth())
    }

}