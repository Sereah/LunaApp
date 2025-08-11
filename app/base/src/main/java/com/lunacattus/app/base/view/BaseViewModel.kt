package com.lunacattus.app.base.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<INTENT : IUIIntent, STATE : IUIState, EFFECT : IUIEffect> :
    ViewModel() {

    abstract val initUiState: STATE

    abstract fun processUiIntent(intent: INTENT)

    private val _uiState = MutableStateFlow(initUiState)
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<EFFECT>()
    val uiEffect = _uiEffect.receiveAsFlow()

    /**
     * 提供给BaseFragment分发UIIntent事件，子viewmodel处理事件
     */
    fun handleUiIntent(intent: INTENT) {
        viewModelScope.launch {
            processUiIntent(intent)
        }
    }

    /**
     * 子viewmodel更新UIState
     */
    protected fun updateUiState(update: (STATE) -> STATE) {
        val currentState = _uiState.value
        val newState = update(currentState)
        _uiState.value = newState
    }

    /**
     * 子viewmodel发送一次性事件
     */
    protected fun sendSideEffect(effect: EFFECT) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}