package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.app.connection.domain.BluetoothRepository
import com.lunacattus.app.connection.ui.ActivityEvent
import com.lunacattus.app.connection.ui.ActivitySideEffect
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val repository: BluetoothRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<BluetoothSideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()
    private val _selectedDevice = MutableStateFlow<BondDevice?>(null)
    val selectedDevice = _selectedDevice.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                repository.state.collect {
                    Logger.d(TAG, "collect bt state: $it")
                    reduce { copy(btState = it) }
                    if (it == BluetoothAdapter.STATE_ON) {
                        loadInfo()
                    }
                }
            }
            launch {
                repository.isDiscovery.collect {
                    Logger.d(TAG, "collect isDiscovery: $it")
                    reduce { copy(discovery = it) }
                }
            }
            launch {
                repository.foundDevices.collect { list ->
                    Logger.d(TAG, "collect discoveryDeviceList: $list")
                    reduce { copy(discoveryDeviceList = list.map { DiscoveryDevice(it) }) }
                }
            }
            launch {
                repository.deviceConnectStateChange.collectLatest { (address, isConnected) ->
                    Logger.d(TAG, "collect deviceConnectStateChange: $address")
                    reduce {
                        copy(bondedDeviceList = bondedDeviceList.map {
                            if (it.device.address == address) {
                                it.copy(isConnected = isConnected)
                            } else {
                                it
                            }
                        })
                    }
                    if (address == _selectedDevice.value?.device?.address) {
                        _selectedDevice.update {
                            it?.copy(
                                isConnected = isConnected,
                                connecting = false,
                                disconnecting = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun processUiIntent(intent: BluetoothUiIntent) {
        when (intent) {
            is BluetoothUiIntent.SwitchEnable -> switchEnable()
            is BluetoothUiIntent.Discovery -> startDiscovery(intent.enable)
            is BluetoothUiIntent.PairNewDevice -> pairNewDevice(intent.device)
            is BluetoothUiIntent.LoadBondedDevices -> getBondedDevices()
            is BluetoothUiIntent.LoadInfo -> loadInfo()
            is BluetoothUiIntent.OnClickDeviceSetting -> {
                _selectedDevice.update { intent.device }
            }

            is BluetoothUiIntent.DisconnectDevice -> {
                _selectedDevice.update { it?.copy(connecting = false, disconnecting = true) }
                repository.disconnectDevice(intent.device.device)
            }

            is BluetoothUiIntent.ConnectDevice -> {
                _selectedDevice.update { it?.copy(connecting = true, disconnecting = false) }
                repository.connectDevice(intent.device.device)
            }

            is BluetoothUiIntent.ForgetDevice -> {
                repository.forgetDevice(intent.device.device)
                _selectedDevice.update { null }
            }
            is BluetoothUiIntent.RequestUuid -> requestUuid(intent.device)
        }
    }

    private fun switchEnable() {
        val isEnable = _uiState.value.btState == BluetoothAdapter.STATE_ON
        reduce {
            copy(
                btState = if (isEnable) {
                    BluetoothAdapter.STATE_TURNING_OFF
                } else {
                    BluetoothAdapter.STATE_TURNING_ON
                }
            )
        }
        repository.switchEnable(!isEnable)
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
                _sideEffect.emit(BluetoothSideEffect.BackDiscoveryScreen)
            }
        }
    }

    private fun requestUuid(device: BondDevice) {
        viewModelScope.launch {
            val result = repository.fetchUuids(device.device)
            Logger.d(TAG, "requestUuid: $result")
        }
    }

    private fun getBondedDevices() {
        reduce {
            copy(bondedDeviceList = repository.getBondedDevices().map {
                BondDevice(it, it.isConnected())
            })
        }
    }

    private fun loadInfo() {
        viewModelScope.launch {
            reduce { copy(loading = true) }
            try {
                val profiles = repository.getBluetoothProfile()
                val address = repository.getAddress()
                val name = repository.getName()
                reduce {
                    copy(
                        info = info.copy(
                            profiles = profiles,
                            address = address,
                            name = name
                        )
                    )
                }
            } catch (e: Exception) {
                ActivitySideEffect.send(ActivityEvent.LogError(e))
            } finally {
                reduce { copy(loading = false) }
            }
        }
    }

    private fun reduce(reducer: BluetoothUiState.() -> BluetoothUiState) {
        _uiState.update(reducer)
    }

    companion object {
        private const val TAG = "BluetoothViewModel"
    }
}
