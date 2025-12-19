package com.lunacattus.nav3test.ui.section.bluetooth.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.logger.Logger
import com.lunacattus.nav3test.domain.bluetooth.BluetoothRepository
import com.lunacattus.nav3test.ui.ActivityEvent
import com.lunacattus.nav3test.ui.ActivityToastEvent
import com.lunacattus.nav3test.ui.section.bluetooth.DiscoveryDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothDiscoveryViewModel @Inject constructor(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothDiscoveryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init")
        viewModelScope.launch {
            launch {
                repository.foundDevices.collect { list ->
                    Logger.d(
                        TAG,
                        "collect discoveryDeviceList: $list"
                    )
                    reduce { copy(discoveryDeviceList = list.map { DiscoveryDevice(it) }) }
                }
            }
            launch {
                launch {
                    repository.isDiscovery.collect {
                        Logger.d(TAG, "collect isDiscovery: $it")
                        reduce { copy(discovery = it) }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
    }

    fun processUiIntent(uiIntent: BluetoothDiscoveryUiIntent) {
        when (uiIntent) {
            is BluetoothDiscoveryUiIntent.Discovery -> startDiscovery(uiIntent.enable)
            is BluetoothDiscoveryUiIntent.PairNewDevice -> pairNewDevice(uiIntent.device)
        }
    }

    private fun startDiscovery(enable: Boolean) {
        repository.discoveryDevices(enable)
    }

    private fun pairNewDevice(device: DiscoveryDevice) {
        viewModelScope.launch {
            reduce {
                copy(discoveryDeviceList = discoveryDeviceList.map {
                    if (it.device.address == device.device.address) {
                        it.copy(isBonding = true)
                    } else {
                        it
                    }
                })
            }
            val success = repository.pairDevice(device.device)
            if (success) {
                ActivityToastEvent.send(ActivityEvent.ShowToast("配对成功"))
                reduce { copy(successBonded = true) }
            }
        }
    }

    private fun reduce(reducer: BluetoothDiscoveryUiState.() -> BluetoothDiscoveryUiState) {
        _uiState.update(reducer)
    }

    companion object {
        const val TAG = "BluetoothDiscoveryViewModel"
    }
}

data class BluetoothDiscoveryUiState(
    val discoveryDeviceList: List<DiscoveryDevice> = emptyList(),
    val discovery: Boolean = false,
    val successBonded: Boolean = false,
)

sealed interface BluetoothDiscoveryUiIntent {
    data class Discovery(val enable: Boolean) : BluetoothDiscoveryUiIntent
    data class PairNewDevice(val device: DiscoveryDevice) : BluetoothDiscoveryUiIntent
}