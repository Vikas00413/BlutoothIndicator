package org.example.project.presentation.ble

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.permissions.BluetoothPermissions
import org.koin.androidx.compose.koinViewModel

@Composable
fun BluetoothBleScreen(
    viewModel: BluetoothBleViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = remember { BluetoothPermissions.requiredRuntimePermissions() }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        val bluetoothGranted = BluetoothPermissions.hasBluetoothPermissions()
        if (bluetoothGranted) {
            showPermissionDialog = false
            viewModel.resetForegroundSession()
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (!BluetoothPermissions.hasBluetoothPermissions()) {
            permissionLauncher.launch(permissions)
        }
    }

    fun requestScan() {
        if (BluetoothPermissions.hasBluetoothPermissions()) {
            viewModel.resetForegroundSession()
            viewModel.startScan()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Bluetooth permission required") },
            text = {
                Text(
                    "This app needs Bluetooth and location permissions to scan nearby BLE devices.",
                )
            },
            confirmButton = {
                Button(onClick = { permissionLauncher.launch(permissions) }) {
                    Text("Grant permissions")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "BLE Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = when {
                        uiState.isConnected -> "Connected to ${uiState.connectedAddress}"
                        uiState.connectingAddress != null -> "Connecting to ${uiState.connectingAddress}..."
                        uiState.isScanning -> "Scanning for devices..."
                        else -> "Tap Start Scan to discover BLE devices"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { requestScan() },
                        enabled = !uiState.isScanning,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Start Scan")
                    }
                    OutlinedButton(
                        onClick = viewModel::stopScan,
                        enabled = uiState.isScanning,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Stop")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = viewModel::disconnect,
                        enabled = uiState.isConnected,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Disconnect")
                    }
                    OutlinedButton(
                        onClick = viewModel::clearDiscovered,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        uiState.lastMessage?.let { message ->
            Text(
                text = "Last message: $message",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = "Discovered devices (${uiState.devices.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (uiState.devices.isEmpty()) {
            Text(
                text = "No BLE devices found yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(uiState.devices, key = { it.address }) { device ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (BluetoothPermissions.hasBluetoothPermissions()) {
                                    viewModel.resetForegroundSession()
                                    viewModel.connect(device.address)
                                } else {
                                    permissionLauncher.launch(permissions)
                                }
                            }
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = device.name.ifBlank { "Unknown device" },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = device.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "RSSI ${device.rssi}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
