package org.example.project.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.project.domain.model.BluetoothDevice
import org.example.project.domain.model.DeviceSyncStatus
import org.example.project.domain.model.DomainConnectionStatus
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BluetoothScreen(
    viewModel: BluetoothViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BluetoothScreenContent(
        uiState = uiState,
        onRefresh = viewModel::refreshFromRemote,
        onAddDevice = viewModel::addDevice,
        onToggleShowPendingOnly = viewModel::toggleShowPendingOnly,
    )
}

@Composable
private fun BluetoothScreenContent(
    uiState: BluetoothUiState,
    onRefresh: () -> Unit,
    onAddDevice: () -> Unit,
    onToggleShowPendingOnly: () -> Unit,
) {
    val displayedDevices = uiState.displayedDevices
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderCard(
            deviceCount = uiState.devices.size,
            pendingCount = uiState.pendingCount,
            isOnline = uiState.isOnline,
            showPendingOnly = uiState.showPendingOnly,
            isLoading = uiState.isLoading,
            isSaving = uiState.isSaving,
            errorMessage = uiState.errorMessage,
            saveMessage = uiState.saveMessage,
            syncStatusMessage = uiState.syncStatusMessage,
            onRefresh = onRefresh,
            onAddDevice = onAddDevice,
            onToggleShowPendingOnly = onToggleShowPendingOnly,
        )

        if (displayedDevices.isEmpty() && !uiState.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Text(
                    text = if (uiState.showPendingOnly) {
                        "No pending devices. All changes are synced."
                    } else {
                        "No devices found. Tap \"Add Device\" to create one."
                    },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(displayedDevices, key = { deviceListKey(it) }) { device ->
                    DeviceCard(
                        device = device,
                        isHighlighted = deviceHighlightKey(device) == uiState.highlightedDeviceKey,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    deviceCount: Int,
    pendingCount: Int,
    isOnline: Boolean,
    showPendingOnly: Boolean,
    isLoading: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    saveMessage: String?,
    syncStatusMessage: String?,
    onRefresh: () -> Unit,
    onAddDevice: () -> Unit,
    onToggleShowPendingOnly: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Bluetooth Devices",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Total devices: $deviceCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOnline) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                    Text(
                        text = "Pending sync: $pendingCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                NetworkStatusChip(isOnline = isOnline)
            }

            syncStatusMessage?.let { message ->
                Surface(
                    color = if (isOnline) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOnline) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Show pending only",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = showPendingOnly,
                    onCheckedChange = { onToggleShowPendingOnly() },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onRefresh,
                    enabled = !isLoading && !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isLoading) "Refreshing..." else "Refresh")
                }

                Button(
                    onClick = onAddDevice,
                    enabled = !isLoading && !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (isSaving) "Adding..." else "Add Device")
                }
            }

            if (isLoading || isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            saveMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: BluetoothDevice,
    isHighlighted: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        border = if (isHighlighted) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = device.bleName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (isHighlighted) {
                        NewBadge()
                    }
                    if (device.syncStatus != DeviceSyncStatus.SYNCED) {
                        PendingBadge(status = device.syncStatus)
                    }
                }
                StatusChip(status = device.connectionStatus)
            }

            Text(
                text = device.ssid,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = buildString {
                    append("Created: ${formatDateTime(device.createdAt)}")
                    append("  •  Last seen: ${formatTimestamp(device.lastSeenTime)}")
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (device.syncStatus != DeviceSyncStatus.SYNCED) {
                Text(
                    text = syncStatusLabel(device.syncStatus),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun NetworkStatusChip(isOnline: Boolean) {
    Surface(
        color = if (isOnline) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = if (isOnline) "ONLINE" else "OFFLINE",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isOnline) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
        )
    }
}

@Composable
private fun PendingBadge(status: DeviceSyncStatus) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = when (status) {
                DeviceSyncStatus.PENDING_CREATE -> "PENDING"
                DeviceSyncStatus.PENDING_UPDATE -> "UPDATING"
                DeviceSyncStatus.SYNCED -> "SYNCED"
            },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun syncStatusLabel(status: DeviceSyncStatus): String {
    return when (status) {
        DeviceSyncStatus.PENDING_CREATE -> "Waiting to upload to server"
        DeviceSyncStatus.PENDING_UPDATE -> "Waiting to update on server"
        DeviceSyncStatus.SYNCED -> "Synced with server"
    }
}

@Composable
private fun NewBadge() {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = "NEW",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusChip(status: DomainConnectionStatus) {
    val (backgroundColor, textColor) = when (status) {
        DomainConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        DomainConnectionStatus.CONNECTING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        DomainConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"

    value.toLongOrNull()?.let { return formatTimestamp(it) }

    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
    )

    for (pattern in inputPatterns) {
        runCatching {
            SimpleDateFormat(pattern, Locale.US).parse(value)?.time
        }.getOrNull()?.let { return formatTimestamp(it) }
    }

    return value
}
