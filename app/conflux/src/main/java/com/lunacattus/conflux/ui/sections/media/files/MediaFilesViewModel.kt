package com.lunacattus.conflux.ui.sections.media.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.conflux.domain.media.MediaRepository
import com.lunacattus.conflux.ui.sections.media.MediaSourceType
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
    @Assisted val type: MediaSourceType,
    private val recordingFileRepository: RecordingFileRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(type: MediaSourceType): MediaFilesViewModel
    }

    private val _uiState = MutableStateFlow(MediaFilesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init, type: $type")
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
                    copy(mediaFiles = mediaFiles.filterNot { it.file == mediaFile.file })
                }
            }
        }
    }

    private fun updateFiles() {
        reduce { copy(isLoading = true) }
        viewModelScope.launch {
            val dataDeferred = async(Dispatchers.IO) {
                when (type) {
                    MediaSourceType.AppRecording -> {
                        recordingFileRepository.getWavFiles().map { file ->
                            MediaFile(file, recordingFileRepository.getWavDuration(file))
                        }
                    }

                    MediaSourceType.SystemMusic -> {
                        mediaRepository.getAllMusic()
                    }
                }
            }
            val timerDeferred = async { delay(500) }
            val list = dataDeferred.await()
            timerDeferred.await()
            reduce { copy(mediaFiles = list, isLoading = false, hasLoaded = true) }
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
    val mediaFiles: List<MediaFile> = emptyList(),
    val hasLoaded: Boolean = false
)

sealed interface MediaFilesUiIntent {
    data class PlayMedia(val file: MediaFile) : MediaFilesUiIntent
    data class DeleteMedia(val file: MediaFile) : MediaFilesUiIntent
}