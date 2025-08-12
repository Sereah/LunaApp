package com.lunacattus.app.gallery.feature.list.mvi

import com.lunacattus.app.base.view.IUIIntent

sealed interface ListUiIntent : IUIIntent {
    data object Init : ListUiIntent
}