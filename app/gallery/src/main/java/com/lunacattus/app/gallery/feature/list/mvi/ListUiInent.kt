package com.lunacattus.app.gallery.feature.list.mvi

import com.lunacattus.app.gallery.base.IUIIntent

sealed interface ListUiIntent : IUIIntent {
    data object Init : ListUiIntent
    data object LoadMoreImage: ListUiIntent
    data object LoadMoreVideo: ListUiIntent
    data object LoadMoreMedia: ListUiIntent
}