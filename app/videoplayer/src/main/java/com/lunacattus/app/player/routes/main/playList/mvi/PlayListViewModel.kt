package com.lunacattus.app.player.routes.main.playList.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.app.data.repository.player.VideoRepository
import com.lunacattus.app.player.routes.main.playList.mvi.PlayListUiState.*
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {
    companion object {
        const val TAG = "PlayListViewModel"
    }

    private val _uiState = MutableStateFlow<PlayListUiState>(Init)
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared.")
    }

    fun handleUiIntent(intent: PlayListUiIntent) {
        when (intent) {
            PlayListUiIntent.Init -> {
                _uiState.update { Loading }
                viewModelScope.launch {
                    videoRepository.queryAllVideo().collect { list ->
                        _uiState.update { PlayListUiState.Success(list) }
                    }
                }
            }

            is PlayListUiIntent.RemoveVideo -> {
                viewModelScope.launch {
                    videoRepository.deleteVideo(intent.video)
                }
            }
        }
    }
}