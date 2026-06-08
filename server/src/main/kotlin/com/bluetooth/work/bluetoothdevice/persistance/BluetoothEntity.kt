package com.bluetooth.work.bluetoothdevice.persistance

import com.bluetooth.work.bluetoothdevice.dto.ConnectionStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant


@Entity
@Table(name = "bluetooth_data")
class BluetoothEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "ssid", nullable = false)
    var ssid: String,

    @Column(name = "ble_name", nullable = true)
    var bluetoothName: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    var connectionStatus: ConnectionStatus,

    @Column(name = "last_seen_time", nullable = false)
    var lastSeenTime: Long,
)