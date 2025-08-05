package com.lunacattus.app.camera.feature.home.mvi

import com.lunacattus.app.camera.base.IUIIntent

sealed interface HomeUiIntent: IUIIntent {
    data object ClickButtonOne: HomeUiIntent
    data object ClickButtonTwo: HomeUiIntent
}