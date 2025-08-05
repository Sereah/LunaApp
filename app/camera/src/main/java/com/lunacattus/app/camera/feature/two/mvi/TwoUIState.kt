package com.lunacattus.app.camera.feature.two.mvi

import com.lunacattus.app.camera.base.IUIState

sealed interface TwoUIState: IUIState {
    data object Init: TwoUIState
    data object Loading: TwoUIState
    data class Success(val date: List<String>): TwoUIState
    data class Error(val msg: String): TwoUIState
}