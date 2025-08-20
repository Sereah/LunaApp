package com.lunacattus.app.player.routes.main.video.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.app.data.repository.player.VideoRepository
import com.lunacattus.app.domain.model.mapper
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Init)
    val uiState = _uiState.asStateFlow()

    companion object {
        const val TAG = "VideoViewModel"
    }

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared.")
    }

    fun handleUiIntent(intent: VideoUiIntent) {
        when (intent) {
            VideoUiIntent.Init -> {
                _uiState.update { VideoUiState.Loading }
                viewModelScope.launch {
                    val videos = videoRepository.getJsonVideos()
                    _uiState.update { VideoUiState.Success(videos) }
                }
            }

            is VideoUiIntent.AddToPlayList -> {
                viewModelScope.launch {
                    videoRepository.insertPlayList(intent.video.mapper())
                }
            }
        }
    }
}