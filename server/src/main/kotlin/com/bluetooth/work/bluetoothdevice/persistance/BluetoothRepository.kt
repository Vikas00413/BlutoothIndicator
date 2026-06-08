package com.bluetooth.work.bluetoothdevice.persistance

import org.springframework.data.jpa.repository.JpaRepository

interface BluetoothRepository : JpaRepository<BluetoothEntity,Long>