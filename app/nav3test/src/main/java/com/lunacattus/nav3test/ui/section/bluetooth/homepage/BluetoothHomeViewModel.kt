package com.lunacattus.nav3test.ui.section.bluetooth.homepage

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.logger.Logger
import com.lunacattus.nav3test.domain.bluetooth.BluetoothRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothHomeViewModel @Inject constructor(
    private val repository: BluetoothRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init")
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
        }
    }

    fun processUiIntent(intent: BluetoothHomeUiIntent) {
        when (intent) {
            is BluetoothHomeUiIntent.LoadInfo -> loadInfo()
            BluetoothHomeUiIntent.SwitchEnable -> switchEnable()
        }
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
    }

    private fun loadInfo() {
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

    private fun reduce(reducer: BluetoothHomeUiState.() -> BluetoothHomeUiState) {
        _uiState.update(reducer)
    }

    companion object {
        const val TAG = "BluetoothHomeViewModel"
    }
}

data class BluetoothHomeUiState(
    val btState: Int = BluetoothAdapter.STATE_OFF,
    val info: BtInfo = BtInfo()
)

sealed interface BluetoothHomeUiIntent {
    data object LoadInfo : BluetoothHomeUiIntent
    data object SwitchEnable : BluetoothHomeUiIntent
}

data class BtInfo(
    val profiles: String = "",
    val address: String = "",
    val name: String = "",
    val uuidList: List<String> = emptyList()
)