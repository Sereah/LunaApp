package com.lunacattus.conflux.ui.sections.media.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.logger.Logger
import com.lunacattus.voice.record.RecordingFileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

@HiltViewModel(assistedFactory = MediaFilesViewModel.Factory::class)
class MediaFilesViewModel @AssistedInject constructor(
    @Assisted val path: String,
    private val recordingFileRepository: RecordingFileRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(path: String): MediaFilesViewModel
    }

    private val _uiState = MutableStateFlow(MediaFilesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init, path: $path")
        updateFiles()
    }

    fun handleUiIntent(intent: MediaFilesUiIntent) {
        Logger.d(TAG, "handleUiIntent: $intent")
        when (intent) {
            is MediaFilesUiIntent.PlayMedia -> playMedia(intent.file)
            is MediaFilesUiIntent.DeleteMedia -> deleteMedia(intent.file)
        }
    }

    private fun playMedia(file: MediaFile) {
        recordingFileRepository.playWithSystemPlayer(file.file)
    }

    private fun deleteMedia(mediaFile: MediaFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = recordingFileRepository.deleteRecursivelySafely(mediaFile.file)
            if (success) {
                reduce {
                    copy(recordFiles = recordFiles.filterNot { it.file == mediaFile.file })
                }
            }
        }
    }

    private fun updateFiles() {
        reduce { copy(isLoading = true) }
        viewModelScope.launch {
            val dataDeferred = async(Dispatchers.IO) {
                recordingFileRepository.getWavFiles(path).map { file ->
                    MediaFile(file, recordingFileRepository.getWavDuration(file))
                }
            }
            val timerDeferred = async { delay(500) }
            val list = dataDeferred.await()
            timerDeferred.await()
            reduce { copy(recordFiles = list, isLoading = false, hasLoaded = true) }
        }
    }

    private fun reduce(reducer: MediaFilesUiState.() -> MediaFilesUiState) {
        _uiState.value = reducer(uiState.value)
    }

    companion object {
        const val TAG = "MediaFilesViewModel"
    }
}

data class MediaFile(
    val file: File,
    val duration: Long
)

data class MediaFilesUiState(
    val isLoading: Boolean = false,
    val recordFiles: List<MediaFile> = emptyList(),
    val hasLoaded: Boolean = false
)

sealed interface MediaFilesUiIntent {
    data class PlayMedia(val file: MediaFile) : MediaFilesUiIntent
    data class DeleteMedia(val file: MediaFile) : MediaFilesUiIntent
}