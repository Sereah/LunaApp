package com.lunacattus.app.connection.routes.main.bluetooth.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.app.connection.domain.BluetoothRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun processUiIntent(intent: BluetoothUiIntent) {
        when (intent) {
            is BluetoothUiIntent.LoadItem -> loadItem(intent.item)
            BluetoothUiIntent.DismissDialog -> reduce { copy(dialogItem = null) }
        }
    }

    private fun loadItem(item: ItemData) {
        viewModelScope.launch {
            reduce { copy(loading = true) }
            try {
                val flow = when (item) {
                    ItemData.Profile -> repository.getBluetoothProfile()
                    ItemData.Address -> repository.getAddress()
                    ItemData.Name -> repository.getName()
                }
                flow.collect { data ->
                    reduce {
                        when (item) {
                            ItemData.Profile -> copy(
                                info = info.copy(profiles = data),
                                dialogItem = item
                            )

                            ItemData.Address -> copy(
                                info = info.copy(address = data),
                                dialogItem = item
                            )

                            ItemData.Name -> copy(info = info.copy(name = data), dialogItem = item)
                        }
                    }
                }
            } catch (e: Exception) {
                _sideEffect.emit(
                    BluetoothSideEffect.ShowToast(
                        e.localizedMessage ?: "Unknown error"
                    )
                )
            } finally {
                reduce { copy(loading = false) }
            }
        }
    }

    private fun reduce(reducer: BluetoothUiState.() -> BluetoothUiState) {
        _uiState.update(reducer)
    }
}