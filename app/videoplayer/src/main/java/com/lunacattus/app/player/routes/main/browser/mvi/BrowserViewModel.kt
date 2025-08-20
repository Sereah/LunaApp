package com.lunacattus.app.player.routes.main.browser.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.app.data.repository.player.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val videoRepository: VideoRepository
): ViewModel() {

    fun handleUiIntent(intent: BrowserUiIntent) {
        when(intent) {
            is BrowserUiIntent.AddStreamToPlayList -> {
                viewModelScope.launch {
                    videoRepository.insertPlayList(intent.video)
                }
            }
        }
    }
}