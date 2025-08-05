package com.lunacattus.app.camera.feature.one.mvi

import com.lunacattus.app.camera.base.IUIIntent

sealed interface OneUiIntent: IUIIntent {
    data class AddData(val name: String): OneUiIntent
}