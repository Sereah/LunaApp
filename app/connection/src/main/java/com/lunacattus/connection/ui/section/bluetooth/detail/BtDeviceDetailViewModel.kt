package com.lunacattus.connection.ui.section.bluetooth.detail

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothUuid
import android.os.ParcelUuid
import androidx.compose.ui.text.style.TextDecoration.Companion.combine
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.connection.domain.bluetooth.BluetoothRepository
import com.lunacattus.connection.domain.bluetooth.HfpProfileRepository
import com.lunacattus.connection.domain.bluetooth.isVendorUuid
import com.lunacattus.connection.domain.bluetooth.name
import com.lunacattus.connection.ui.ActivityToastEvent
import com.lunacattus.connection.ui.ToastEvent
import com.lunacattus.connection.ui.section.bluetooth.BondDevice
import com.lunacattus.connection.ui.section.bluetooth.BondDeviceConnectType
import com.lunacattus.connection.ui.section.bluetooth.DeviceUUID
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BtDeviceDetailViewModel @Inject constructor(
    private val repository: BluetoothRepository,
    private val hfpProfileRepository: HfpProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BtDeviceDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init")
        viewModelScope.launch {
            launch { processDeviceConnectStateChange() }
            launch { fetchProfileList() }
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
            is BtDeviceDetailUiIntent.ChangeProfileConnectState -> changeProfileConnectState(intent.uuid, intent.connect)
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
            }
        }
    }

    private fun changeProfileConnectState(uuid: ParcelUuid, connect: Boolean) {
        viewModelScope.launch {
            _uiState.value.selectDevice?.let { device ->
                when (uuid) {
                    BluetoothUuid.HFP -> {
                        changeHfpConnectState(connect, device.device)
                    }
                }
            }
        }
    }

    private suspend fun changeHfpConnectState(connect: Boolean, device: BluetoothDevice) {
        if (connect) {
            if (!hfpProfileRepository.connect(device)) {
                ActivityToastEvent.send(ToastEvent.ShowToast("HFP连接失败"))
            }
        } else {
            if (!hfpProfileRepository.disconnect(device)) {
                ActivityToastEvent.send(ToastEvent.ShowToast("HFP断开失败"))
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun fetchProfileList() {
        uiState.mapNotNull { it.selectDevice }
            .distinctUntilChanged { old, new -> old.device.address == new.device.address }
            .flatMapLatest { selected ->
                hfpProfileRepository.observeConnectState(selected.device)
            }
            .collect { state ->
                reduce {
                    copy(
                        selectDevice = selectDevice?.copy(
                            uuidList = selectDevice.uuidList.map { uuid ->
                                if (uuid.uuid == BluetoothUuid.HFP) {
                                    uuid.copy(connectState = state)
                                } else {
                                    uuid
                                }
                            }
                        )
                    )
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
    data class ChangeProfileConnectState(val uuid: ParcelUuid, val connect: Boolean) : BtDeviceDetailUiIntent
}