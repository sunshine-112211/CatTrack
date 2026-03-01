package com.cattrack.app.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cattrack.app.bluetooth.BleDataParser
import com.cattrack.app.data.model.*
import com.cattrack.app.data.repository.CatRepository
import com.cattrack.app.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val currentDevice: DeviceInfo? = null,
    val batteryLevel: Int = 0,
    val firmwareUpdateState: FirmwareUpdateState = FirmwareUpdateState(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val catRepository: CatRepository,
    private val bleDataParser: BleDataParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
        viewModelScope.launch {
            deviceRepository.batteryLevel.collect { level ->
                _uiState.update { it.copy(batteryLevel = level) }
            }
        }
        viewModelScope.launch {
            deviceRepository.currentDevice.collect { device ->
                _uiState.update { it.copy(currentDevice = device) }
            }
        }
    }

    fun disconnect() {
        deviceRepository.disconnect()
    }

    fun syncData() {
        viewModelScope.launch {
            // Send sync command
        }
    }

    fun startFirmwareUpdate() {
        _uiState.update {
            it.copy(firmwareUpdateState = FirmwareUpdateState(isUpdating = true, progress = 0))
        }
        viewModelScope.launch {
            // Simulate OTA update progress
            for (progress in 0..100 step 5) {
                kotlinx.coroutines.delay(100)
                _uiState.update {
                    it.copy(firmwareUpdateState = it.firmwareUpdateState.copy(progress = progress))
                }
            }
            _uiState.update {
                it.copy(firmwareUpdateState = FirmwareUpdateState(isUpdating = false, progress = 100))
            }
        }
    }

    fun isBleEnabled(): Boolean = deviceRepository.isBleEnabled()
}
