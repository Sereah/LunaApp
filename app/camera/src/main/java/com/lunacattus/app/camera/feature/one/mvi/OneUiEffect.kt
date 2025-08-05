package com.lunacattus.app.camera.feature.one.mvi

import com.lunacattus.app.camera.base.IUIEffect

sealed interface OneUiEffect: IUIEffect {
    data class ShowToast(val msg: String): OneUiEffect
}