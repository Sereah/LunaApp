package com.lunacattus.app.camera.feature.home.mvi

import com.lunacattus.app.base.view.IUIEffect

sealed interface HomeUiEffect : IUIEffect {
    data object NavToFeatureOne : HomeUiEffect
    data object NavToFeatureTwo : HomeUiEffect
}