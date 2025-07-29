package com.lunacattus.app.presentation.view.feature.one.mvi

import com.lunacattus.app.presentation.view.base.IUIIntent

sealed interface OneUiIntent: IUIIntent {
    data class AddData(val name: String): OneUiIntent
}