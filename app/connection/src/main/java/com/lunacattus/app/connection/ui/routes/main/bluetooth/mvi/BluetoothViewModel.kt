package com.lunacattus.app.connection.ui.routes.main.bluetooth.mvi

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.app.connection.domain.BluetoothRepository
import com.lunacattus.app.connection.domain.name
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
            launch { processDeviceConnectState() }
        }
    }

    fun processUiIntent(intent: BluetoothUiIntent) {
        when (intent) {
            is BluetoothUiIntent.SwitchEnable -> switchEnable()
            is BluetoothUiIntent.Discovery -> startDiscovery(intent.enable)
            is BluetoothUiIntent.PairNewDevice -> pairNewDevice(intent.device)
            is BluetoothUiIntent.LoadBondedDevices -> getBondedDevices()
            is BluetoothUiIntent.LoadInfo -> loadInfo()
            is BluetoothUiIntent.OnClickDeviceSetting -> updateSelectDevice(intent.device)
            is BluetoothUiIntent.DisconnectDevice -> disconnectDevice(intent.device)
            is BluetoothUiIntent.ConnectDevice -> connectDevice(intent.device)
            is BluetoothUiIntent.ForgetDevice -> forgetDevice(intent.device)
            is BluetoothUiIntent.RequestUuid -> requestUuid(intent.device)
            is BluetoothUiIntent.ConnectVendorUuid -> connectVendorUuid(
                intent.address,
                intent.deviceUUID
            )
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
                ActivitySideEffect.send(ActivityEvent.ShowToast("配对成功"))
                _sideEffect.emit(BluetoothSideEffect.BackDiscoveryScreen)
            }
        }
    }

    private fun requestUuid(device: BondDevice) {
        val uuidList = device.device.uuids?.map { uuid ->
            DeviceUUID(
                name = uuid.name(),
                uuid = uuid
            )
        }
        if (uuidList == null) {
            viewModelScope.launch {
                val result = repository.fetchUuids(device.device)
                Logger.d(TAG, "requestUuid: $result")
            }
        } else {
            reduce {
                copy(currentDetailDevice = currentDetailDevice?.copy(uuidList = uuidList))
            }
        }
    }

    private fun connectVendorUuid(address: String, deviceUUID: DeviceUUID) {
        viewModelScope.launch {
            repository.connectRfcomm(address, deviceUUID.uuid.uuid).collect {
                Logger.d(TAG, "connect rfcomm result: $it")
            }
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

    private fun updateSelectDevice(device: BondDevice) {
        reduce {
            copy(currentDetailDevice = device)
        }
    }

    private fun disconnectDevice(device: BondDevice) {
        reduce {
            copy(
                bondedDeviceList = bondedDeviceList.map {
                    if (device.device.address == it.device.address) {
                        it.copy(connectType = BondDeviceConnectType.Disconnecting)
                    } else it
                },
                currentDetailDevice = if (device.device.address == currentDetailDevice?.device?.address) {
                    currentDetailDevice.copy(connectType = BondDeviceConnectType.Disconnecting)
                } else currentDetailDevice
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
                },
                currentDetailDevice = if (device.device.address == currentDetailDevice?.device?.address) {
                    currentDetailDevice.copy(connectType = BondDeviceConnectType.Connecting)
                } else currentDetailDevice
            )
        }
        repository.connectDevice(device.device)
    }

    private fun forgetDevice(device: BondDevice) {
        repository.forgetDevice(device.device)
        reduce {
            copy(currentDetailDevice = null)
        }
    }

    private suspend fun processDeviceConnectState() {
        repository.deviceConnectStateChange.collectLatest { (address, isConnected) ->
            Logger.d(TAG, "collect deviceConnectStateChange: $address, isConnected: $isConnected")
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
                    },
                    currentDetailDevice = if (address == currentDetailDevice?.device?.address) {
                        currentDetailDevice.copy(connectType = connectType)
                    } else currentDetailDevice
                )
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
