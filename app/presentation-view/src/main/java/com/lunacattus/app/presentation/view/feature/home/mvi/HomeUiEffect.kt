package com.lunacattus.app.presentation.view.feature.home.mvi

import com.lunacattus.app.presentation.view.base.IUIEffect

sealed interface HomeUiEffect : IUIEffect {
    data object NavToFeatureOne : HomeUiEffect
    data object NavToFeatureTwo : HomeUiEffect
}