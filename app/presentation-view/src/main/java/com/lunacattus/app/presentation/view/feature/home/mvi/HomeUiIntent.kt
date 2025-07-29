package com.lunacattus.app.presentation.view.feature.home.mvi

import com.lunacattus.app.presentation.view.base.IUIIntent

sealed interface HomeUiIntent: IUIIntent {
    data object ClickButtonOne: HomeUiIntent
    data object ClickButtonTwo: HomeUiIntent
}