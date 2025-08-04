package com.lunacattus.app.presentation.compose.routes.player.mvi

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private val _mediaItems= MutableStateFlow(MediaItems())
    val mediaItems = _mediaItems.asStateFlow()

    companion object {
        const val TAG = "PlayerViewModel"
    }

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared.")
    }

    fun setPlayList(mediaItems: MediaItems) {
        _mediaItems.value = mediaItems
    }
}

data class MediaItems(
    val list: List<MediaItem> = emptyList(),
    val startIndex: Int = 0,
)