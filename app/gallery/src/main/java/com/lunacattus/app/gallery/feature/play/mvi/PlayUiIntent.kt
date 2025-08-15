package com.lunacattus.app.gallery.feature.play.mvi

import com.lunacattus.app.base.view.base.IUIIntent

sealed interface PlayUiIntent : IUIIntent {
    data object Init : PlayUiIntent
}