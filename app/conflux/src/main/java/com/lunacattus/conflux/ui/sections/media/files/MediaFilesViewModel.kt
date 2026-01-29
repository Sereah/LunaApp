package com.lunacattus.conflux.ui.sections.media.files

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lunacattus.conflux.domain.media.MediaRepository
import com.lunacattus.conflux.domain.media.PlayerManager
import com.lunacattus.conflux.ui.sections.media.MediaSourceType
import com.lunacattus.conflux.ui.sections.media.files.MediaFileItem.Companion.toMediaFileItem
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
    private val mediaRepository: MediaRepository,
    private val playerManager: PlayerManager
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

    private fun playMedia(file: MediaFileItem) {
        playerManager.play(file.mediaItem)
    }

    private fun deleteMedia(mediaFile: MediaFileItem) {

    }

    private fun updateFiles() {
        reduce { copy(isLoading = true) }
        viewModelScope.launch {
            val dataDeferred = async(Dispatchers.IO) {
                when (type) {
                    MediaSourceType.AppRecording -> {
                        recordingFileRepository.getWavFiles().map { file ->
                            file.toMediaFileItem(duration = recordingFileRepository.getWavDuration(file))
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

data class MediaFileItem(
    val mediaItem: MediaItem,
    val size: Long,
    val dateModified: Long,
    val isLocalPrivate: Boolean,
    val mimeType: String,
    val width: Int = 0,       // 图片/视频特有
    val height: Int = 0,      // 图片/视频特有
    val duration: Long = 0,   // 音频/视频特有
) {
    val isImage: Boolean get() = mimeType.startsWith("image/")
    val isAudio: Boolean get() = mimeType.startsWith("audio/")
    val isVideo: Boolean get() = mimeType.startsWith("video/")

    companion object {
        fun File.toMediaFileItem(width: Int = 0, height: Int = 0, duration: Long = 0L): MediaFileItem {
            val mediaItem = MediaItem.Builder()
                .setMediaId(this.hashCode().toString())
                .setUri(Uri.fromFile(this))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(this.name)
                        .setIsPlayable(true)
                        .build()
                )
                .build()

            return MediaFileItem(
                mediaItem = mediaItem,
                size = this.length(),
                dateModified = this.lastModified(),
                isLocalPrivate = true,
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension.lowercase()) ?: "",
                duration = duration
            )
        }
    }
}

data class MediaFilesUiState(
    val isLoading: Boolean = false,
    val mediaFiles: List<MediaFileItem> = emptyList(),
    val hasLoaded: Boolean = false
)

sealed interface MediaFilesUiIntent {
    data class PlayMedia(val file: MediaFileItem) : MediaFilesUiIntent
    data class DeleteMedia(val file: MediaFileItem) : MediaFilesUiIntent
}