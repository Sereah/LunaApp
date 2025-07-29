package com.lunacattus.app.presentation.view.feature.one.mvi

import com.lunacattus.app.presentation.view.base.IUIEffect

sealed interface OneUiEffect: IUIEffect {
    data class ShowToast(val msg: String): OneUiEffect
}