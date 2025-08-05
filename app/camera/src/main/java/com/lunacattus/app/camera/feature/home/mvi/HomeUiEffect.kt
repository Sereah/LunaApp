package com.lunacattus.app.camera.feature.home.mvi

import com.lunacattus.app.camera.base.IUIEffect

sealed interface HomeUiEffect : IUIEffect {
    data object NavToFeatureOne : HomeUiEffect
    data object NavToFeatureTwo : HomeUiEffect
}