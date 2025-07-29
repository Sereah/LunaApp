package com.lunacattus.app.presentation.compose.routes.home.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Init)
    val uiState = _uiState.asStateFlow()

    companion object {
        const val TAG = "HomeViewModel"
    }

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared.")
    }

    fun handleUiIntent(intent: HomeUiIntent) {
        when (intent) {
            HomeUiIntent.Start -> {
                viewModelScope.launch {
                    delay(1000)
                    _uiState.update { HomeUiState.Loading }
                    delay(1000)
                    _uiState.update { HomeUiState.Success("Success---First") }
                    delay(1000)
                    _uiState.update { HomeUiState.Success("Success---Second") }
                    delay(1000)
                    _uiState.update { HomeUiState.Fail("Fail") }
                }
            }
        }
    }
}