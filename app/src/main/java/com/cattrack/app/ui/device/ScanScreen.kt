package com.cattrack.app.ui.device

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cattrack.app.data.model.ConnectionState
import com.cattrack.app.data.model.ScannedDevice
import com.cattrack.app.data.repository.DeviceRepository
import com.cattrack.app.ui.theme.ActiveGreen
import com.cattrack.app.ui.theme.CatOrange
import com.google.accompanist.permissions.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : androidx.lifecycle.ViewModel() {
    val scannedDevices = deviceRepository.scannedDevices
    val connectionState = deviceRepository.connectionState

    fun startScan() = deviceRepository.startScan()
    fun stopScan() = deviceRepository.stopScan()
    fun connect(address: String) {
        viewModelScope.launch {
            deviceRepository.connectDevice(address)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    val blePermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描设备", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!blePermissions.allPermissionsGranted) {
                PermissionRequiredCard(onRequest = { blePermissions.launchMultiplePermissionRequest() })
                return@Column
            }

            // Scan Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startScan() },
                    modifier = Modifier.weight(1f),
                    enabled = connectionState != ConnectionState.SCANNING
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (connectionState == ConnectionState.SCANNING) "扫描中..." else "开始扫描")
                }
                OutlinedButton(
                    onClick = { viewModel.stopScan() },
                    modifier = Modifier.weight(1f),
                    enabled = connectionState == ConnectionState.SCANNING
                ) {
                    Text("停止扫描")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (connectionState == ConnectionState.SCANNING) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (scannedDevices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📡", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("未找到设备", style = MaterialTheme.typography.bodyLarge)
                        Text("请确保设备已开机并在附近", color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Text(
                    text = "发现 ${scannedDevices.size} 个设备",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(scannedDevices) { device ->
                        ScannedDeviceCard(
                            device = device,
                            isConnecting = connectionState == ConnectionState.CONNECTING,
                            onConnect = { viewModel.connect(device.address) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannedDeviceCard(
    device: ScannedDevice,
    isConnecting: Boolean,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                tint = CatOrange,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "信号: ${device.rssi} dBm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onConnect,
                enabled = !isConnecting
            ) {
                Text(if (isConnecting) "连接中..." else "连接")
            }
        }
    }
}

@Composable
private fun PermissionRequiredCard(onRequest: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔐", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("需要蓝牙权限", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("扫描设备需要蓝牙和位置权限", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRequest) {
                Text("授权权限")
            }
        }
    }
}
