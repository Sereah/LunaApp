package com.lunacattus.app.gallery.feature.play.mvi

import com.lunacattus.app.gallery.base.IUIIntent

sealed interface PlayUiIntent : IUIIntent {
    data object Init : PlayUiIntent
}