package com.lunacattus.app.camera.feature.one.mvi

import com.lunacattus.app.camera.base.IUIState
import com.lunacattus.app.domain.model.Data

sealed interface OneUIState : IUIState {
    data object Init : OneUIState
    data object Loading : OneUIState
    data class Success(val dataList: List<Data>) : OneUIState
    data class Error(val msg: String) : OneUIState
}