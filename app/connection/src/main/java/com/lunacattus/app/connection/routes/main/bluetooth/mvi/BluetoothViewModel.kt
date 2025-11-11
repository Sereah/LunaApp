package com.lunacattus.app.connection.routes.main.bluetooth.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.app.data.repository.connection.BluetoothRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _uiEffect = MutableSharedFlow<BluetoothUiEffect>()
    val uiEffect = _uiEffect

    fun handleUiIntent(intent: BluetoothUiIntent) {
        when (intent) {
            BluetoothUiIntent.GetBluetoothProfile -> {
                viewModelScope.launch {
                    repository.getBluetoothProfile().collect { profile ->
                        _uiState.update { it.copy(profiles = profile) }
                    }
                }
            }

            BluetoothUiIntent.GetAddress -> {
                viewModelScope.launch {
                    repository.getAddress().collect { a ->
                        _uiState.update { it.copy(address = a) }
                    }
                }
            }

            BluetoothUiIntent.GetName -> {
                viewModelScope.launch {
                    repository.getName().collect { n ->
                        _uiState.update { it.copy(name = n) }
                    }
                }
            }
        }
    }

}