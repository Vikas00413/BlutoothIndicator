package com.example.iotpoc.bluetooth.integration

/**
 * Example: how the host app UI layer should initialize and consume the library.
 * Copy this pattern into your Application / ViewModel / Composable.
 *
 * ```kotlin
 * // 1) Application.onCreate()
 * class IotPocApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         BluetoothLibrary.initialize(this)
 *     }
 * }
 *
 * // 2) ViewModel (UI only observes flows)
 * class DeviceViewModel : ViewModel() {
 *     private val bluetooth = BluetoothLibrary.requireController()
 *
 *     val connectionState = bluetooth.connectionState
 *     val savedDevices = bluetooth.savedDevices
 *     val discoveredDevices = bluetooth.discoveredDevices
 *     val logs = bluetooth.logs
 *
 *     fun onStart() = viewModelScope.launch {
 *         val permissions = BluetoothLibrary.permissionsOrNull()
 *         if (permissions?.hasAllPermissions() == true) {
 *             bluetooth.startForegroundSession()
 *             bluetooth.startScan()
 *         }
 *     }
 *
 *     fun onDeviceClick(address: String, name: String?) = viewModelScope.launch {
 *         bluetooth.connect(address, name)
 *     }
 * }
 *
 * // 3) Compose UI
 * @Composable
 * fun DeviceScreen(vm: DeviceViewModel = hiltViewModel()) {
 *     val state by vm.connectionState.collectAsState()
 *     val devices by vm.discoveredDevices.collectAsState()
 *
 *     LaunchedEffect(Unit) {
 *         vm.logs.collect { entry ->
 *             Log.d(entry.tag, "[${entry.level}] ${entry.message}")
 *         }
 *     }
 *
 *     Text("Status: $state")
 *     LazyColumn {
 *         items(devices) { device ->
 *             Text("${device.name} (${device.address})")
 *         }
 *     }
 * }
 *
 * // 4) Activity — request runtime permissions (UI responsibility)
 * val permissionLauncher = rememberLauncherForActivityResult(
 *     ActivityResultContracts.RequestMultiplePermissions()
 * ) { result ->
 *     if (result.values.all { it }) {
 *         viewModel.onStart()
 *     }
 * }
 *
 * val missing = BluetoothLibrary.permissionsOrNull()?.missingPermissions().orEmpty()
 * if (missing.isNotEmpty()) {
 *     permissionLauncher.launch(missing.toTypedArray())
 * }
 * ```
 */
internal object BluetoothLibraryIntegrationExample
