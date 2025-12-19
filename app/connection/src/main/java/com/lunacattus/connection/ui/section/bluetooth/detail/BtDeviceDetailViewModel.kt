package com.lunacattus.connection.ui.section.bluetooth.detail

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.connection.domain.bluetooth.BluetoothRepository
import com.lunacattus.connection.domain.bluetooth.HfpProfileRepository
import com.lunacattus.connection.model.bluetooth.BondDevice
import com.lunacattus.connection.model.bluetooth.BondDeviceConnectType
import com.lunacattus.logger.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = BtDeviceDetailViewModel.Factory::class)
@SuppressLint("MissingPermission")
class BtDeviceDetailViewModel @AssistedInject constructor(
    @Assisted val address: String,
    private val repository: BluetoothRepository,
    private val hfpProfileRepository: HfpProfileRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(address: String): BtDeviceDetailViewModel
    }

    private val _uiState = MutableStateFlow(BtDeviceDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init, address: $address")
        loadDevice(address)
        viewModelScope.launch {
            loadProfileList()
        }
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
    }

    fun processUiIntent(intent: BtDeviceDetailUiIntent) {
        when (intent) {
            is BtDeviceDetailUiIntent.ConnectDevice -> connectDevice()
            is BtDeviceDetailUiIntent.DisconnectDevice -> disconnectDevice()
            is BtDeviceDetailUiIntent.ForgetDevice -> forgetDevice()
        }
    }

    private fun loadDevice(address: String) {
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
    }

    private fun connectDevice() {
        uiState.value.selectDevice?.let { device ->
            reduce {
                copy(selectDevice = selectDevice?.copy(connectType = BondDeviceConnectType.Connecting))
            }
            repository.connectDevice(device.device)
        }
    }

    private fun disconnectDevice() {
        uiState.value.selectDevice?.let { device ->
            reduce {
                copy(selectDevice = selectDevice?.copy(connectType = BondDeviceConnectType.Disconnecting))
            }
            repository.disconnectDevice(device.device)
        }
    }

    private fun forgetDevice() {
        uiState.value.selectDevice?.let { device ->
            repository.forgetDevice(device.device)
        }
    }

    private suspend fun loadProfileList() {
        val device = uiState.map { it.selectDevice?.device }.filterNotNull().first()
        repository.fetchDeviceUuids(device).collect {
            reduce {
                copy(
                    selectDevice = selectDevice?.copy(
                        uuidList = it
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
    data object ConnectDevice : BtDeviceDetailUiIntent
    data object DisconnectDevice : BtDeviceDetailUiIntent
    data object ForgetDevice : BtDeviceDetailUiIntent
}