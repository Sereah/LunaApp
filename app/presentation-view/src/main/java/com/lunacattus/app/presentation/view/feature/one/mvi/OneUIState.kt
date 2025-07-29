package com.lunacattus.app.presentation.view.feature.one.mvi

import com.lunacattus.app.domain.model.Data
import com.lunacattus.app.presentation.view.base.IUIState

sealed interface OneUIState : IUIState {
    data object Init : OneUIState
    data object Loading : OneUIState
    data class Success(val dataList: List<Data>) : OneUIState
    data class Error(val msg: String) : OneUIState
}