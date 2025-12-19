package com.lunacattus.connection.ui.section.bluetooth.detail

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.connection.domain.bluetooth.BluetoothRepository
import com.lunacattus.connection.domain.bluetooth.isVendorUuid
import com.lunacattus.connection.domain.bluetooth.name
import com.lunacattus.connection.ui.ActivityToastEvent
import com.lunacattus.connection.ui.ToastEvent
import com.lunacattus.connection.ui.section.bluetooth.BondDevice
import com.lunacattus.connection.ui.section.bluetooth.BondDeviceConnectType
import com.lunacattus.connection.ui.section.bluetooth.DeviceUUID
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BtDeviceDetailViewModel @Inject constructor(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BtDeviceDetailUiState())
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

    fun processUiIntent(intent: BtDeviceDetailUiIntent) {
        when (intent) {
            is BtDeviceDetailUiIntent.GetDevice -> getDevice(intent.address)
            is BtDeviceDetailUiIntent.ConnectDevice -> connectDevice()
            is BtDeviceDetailUiIntent.DisconnectDevice -> disconnectDevice()
            is BtDeviceDetailUiIntent.ForgetDevice -> forgetDevice()
            is BtDeviceDetailUiIntent.ConnectUuid -> connectUuid(intent.deviceUUID)
        }
    }

    private fun getDevice(address: String) {
        val device = repository.getRemoteDevice(address)
        reduce {
            copy(
                selectDevice = BondDevice(
                    device = device,
                    connectType = if (device.isConnected()) {
                        BondDeviceConnectType.Connected
                    } else {
                        BondDeviceConnectType.Disconnected
                    }
                )
            )
        }
        fetchUuidList(device)
    }

    private fun disconnectDevice() {
        uiState.value.selectDevice?.let { device ->
            reduce {
                copy(selectDevice = selectDevice?.copy(connectType = BondDeviceConnectType.Disconnecting))
            }
            repository.disconnectDevice(device.device)
        }
    }

    private fun connectDevice() {
        uiState.value.selectDevice?.let { device ->
            reduce {
                copy(selectDevice = selectDevice?.copy(connectType = BondDeviceConnectType.Connecting))
            }
            repository.connectDevice(device.device)
        }
    }

    private fun forgetDevice() {
        uiState.value.selectDevice?.let { device ->
            repository.forgetDevice(device.device)
        }
    }

    private fun connectUuid(deviceUUID: DeviceUUID) {
        uiState.value.selectDevice?.let { device ->
            viewModelScope.launch {
                if (deviceUUID.uuid.isVendorUuid()) {
                    repository.connectRfcomm(device.device.address, deviceUUID.uuid.uuid).collect {
                        Logger.d(TAG, "connect rfcomm result: $it")
                    }
                }
                when (deviceUUID.uuid) {
                    BluetoothUuid.HFP -> {

                    }
                }
            }
        }
    }

    private suspend fun processDeviceConnectStateChange() {
        repository.deviceConnectStateChange.collectLatest { (address, isConnected) ->
            Logger.d(TAG, "collect deviceConnectStateChange: $address, isConnected: $isConnected")
            val connectType = if (isConnected) {
                BondDeviceConnectType.Connected
            } else {
                BondDeviceConnectType.Disconnected
            }
            reduce {
                copy(selectDevice = selectDevice?.copy(connectType = connectType))
            }
        }
    }

    private fun fetchUuidList(device: BluetoothDevice) {
        val uuids = device.uuids?.map { uuid -> DeviceUUID(name = uuid.name(), uuid = uuid) }
        if (uuids != null) {
            reduce { copy(selectDevice = selectDevice?.copy(uuidList = uuids)) }
        } else {
            viewModelScope.launch {
                ActivityToastEvent.send(ToastEvent.ShowToast("缓存的UUID为空，发起SDP"))
                repository.fetchDeviceUuids(device).collectLatest { uuids ->
                    Logger.d(TAG, "collect uuid list: $uuids")
                    reduce {
                        copy(selectDevice = selectDevice?.copy(uuidList = uuids.map { uuid ->
                            DeviceUUID(name = uuid.name(), uuid = uuid)
                        }))
                    }
                }
            }
        }
    }

    private fun reduce(reducer: BtDeviceDetailUiState.() -> BtDeviceDetailUiState) {
        _uiState.update(reducer)
    }

    companion object {
        const val TAG = "BtDeviceDetailViewModel"
    }
}

data class BtDeviceDetailUiState(
    val selectDevice: BondDevice? = null
)

sealed interface BtDeviceDetailUiIntent {
    data class GetDevice(val address: String) : BtDeviceDetailUiIntent
    data object ConnectDevice : BtDeviceDetailUiIntent
    data object DisconnectDevice : BtDeviceDetailUiIntent
    data object ForgetDevice : BtDeviceDetailUiIntent
    data class ConnectUuid(val deviceUUID: DeviceUUID) : BtDeviceDetailUiIntent
}