package com.lunacattus.connection.ui.section.bluetooth.bonded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.logger.Logger
import com.lunacattus.connection.domain.bluetooth.BluetoothRepository
import com.lunacattus.connection.ui.section.bluetooth.BondDevice
import com.lunacattus.connection.ui.section.bluetooth.BondDeviceConnectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothBondedViewModel @Inject constructor(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothBondedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init")
        viewModelScope.launch {
            processDeviceConnectStateChange()
        }
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
    }

    fun processUiIntent(intent: BluetoothBondedUiIntent) {
        when (intent) {
            BluetoothBondedUiIntent.LoadBondedDevices -> getBondedDevices()
            is BluetoothBondedUiIntent.ConnectDevice -> connectDevice(intent.device)
            is BluetoothBondedUiIntent.DisconnectDevice -> disconnectDevice(intent.device)
        }
    }

    private fun getBondedDevices() {
        reduce {
            copy(bondedDeviceList = repository.getBondedDevices().map {
                BondDevice(
                    it, connectType = if (it.isConnected()) {
                        BondDeviceConnectType.Connected
                    } else {
                        BondDeviceConnectType.Disconnected
                    }
                )
            })
        }
    }

    private fun disconnectDevice(device: BondDevice) {
        reduce {
            copy(
                bondedDeviceList = bondedDeviceList.map {
                    if (device.device.address == it.device.address) {
                        it.copy(connectType = BondDeviceConnectType.Disconnecting)
                    } else it
                }
            )
        }
        repository.disconnectDevice(device.device)
    }

    private fun connectDevice(device: BondDevice) {
        reduce {
            copy(
                bondedDeviceList = bondedDeviceList.map {
                    if (device.device.address == it.device.address) {
                        it.copy(connectType = BondDeviceConnectType.Connecting)
                    } else it
                }
            )
        }
        repository.connectDevice(device.device)
    }

    private suspend fun processDeviceConnectStateChange() {
        repository.deviceConnectStateChange.collectLatest { (address, isConnected) ->
            Logger.d(
                TAG,
                "collect deviceConnectStateChange: $address, isConnected: $isConnected"
            )
            val connectType = if (isConnected) {
                BondDeviceConnectType.Connected
            } else {
                BondDeviceConnectType.Disconnected
            }
            reduce {
                copy(
                    bondedDeviceList = bondedDeviceList.map {
                        if (it.device.address == address) {
                            it.copy(connectType = connectType)
                        } else {
                            it
                        }
                    }
                )
            }
        }
    }

    private fun reduce(reducer: BluetoothBondedUiState.() -> BluetoothBondedUiState) {
        _uiState.update(reducer)
    }

    companion object {
        const val TAG = "BluetoothBondedViewModel"
    }
}

data class BluetoothBondedUiState(
    val bondedDeviceList: List<BondDevice> = emptyList(),
)

sealed interface BluetoothBondedUiIntent {
    data object LoadBondedDevices : BluetoothBondedUiIntent
    data class ConnectDevice(val device: BondDevice) : BluetoothBondedUiIntent
    data class DisconnectDevice(val device: BondDevice) : BluetoothBondedUiIntent
}