package com.lunacattus.app.presentation.view.feature.home.mvi

import com.lunacattus.app.presentation.view.base.IUIState

sealed interface HomeUIState : IUIState {
    data object Init : HomeUIState
    data object Loading : HomeUIState
    data class Success(val date: List<String>) : HomeUIState
    data class Error(val msg: String) : HomeUIState
}