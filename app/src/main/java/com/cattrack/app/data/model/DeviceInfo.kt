package com.cattrack.app.data.model

data class DeviceInfo(
    val deviceId: String,
    val deviceName: String,
    val macAddress: String,
    val firmwareVersion: String = "1.0.0",
    val hardwareVersion: String = "1.0",
    val batteryLevel: Int = 0, // 0-100
    val isCharging: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val signalStrength: Int = 0, // RSSI
    val lastConnectedTime: Long = 0L,
    val isBound: Boolean = false
)

enum class ConnectionState(val displayName: String) {
    DISCONNECTED("未连接"),
    SCANNING("扫描中"),
    CONNECTING("连接中"),
    CONNECTED("已连接"),
    BONDING("配对中"),
    ERROR("连接错误")
}

data class ScannedDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnectable: Boolean = true
)

data class FirmwareUpdateState(
    val isUpdating: Boolean = false,
    val progress: Int = 0,
    val currentVersion: String = "",
    val newVersion: String = "",
    val error: String? = null
)
