package com.lunacattus.llm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.common.coroutine.SafeCoroutine.launchSafe
import com.lunacattus.llm.di.LlmCpp
import com.lunacattus.llm.domain.ILlm
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @param:LlmCpp private val llm: ILlm
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    companion object {
        const val TAG = "MainViewModel"
    }

    init {
        Logger.d(TAG, "init")
        initEngine()
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
        super.onCleared()
    }

    private fun initEngine() {
        viewModelScope.launchSafe("initEngine") {
            llm.init().onStart {
                _state.update { it.copy(modelStatus = ModelState.Loading) }
            }.collect { result ->
                result.onSuccess { isSuccess ->
                    if (isSuccess) {
                        _state.update { it.copy(modelStatus = ModelState.Loaded) }
                    } else {
                        handleInitFail("init failed")
                    }
                }.onFailure { thr ->
                    handleInitFail(thr.message ?: "unknown failed")
                }
            }
        }
    }

    private fun handleInitFail(msg: String) {
        _state.update { it.copy(modelStatus = ModelState.Error(msg)) }
    }
}

data class UiState(
    val modelStatus: ModelState = ModelState.Idle
)

sealed class ModelState {
    data object Idle : ModelState()
    data object Loading : ModelState()
    data object Loaded : ModelState()
    data class Error(val msg: String) : ModelState()
}