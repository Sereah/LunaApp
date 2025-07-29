package com.lunacattus.app.presentation.compose.routes.home.mvi

sealed interface HomeUiState {
    data object Init: HomeUiState
    data object Loading: HomeUiState
    data class Success(val msg: String): HomeUiState
    data class Fail(val msg: String): HomeUiState
}