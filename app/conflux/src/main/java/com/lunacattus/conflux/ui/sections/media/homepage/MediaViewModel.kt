package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.logger.Logger
import com.lunacattus.record.AudioRecordManager
import com.lunacattus.record.RecordingFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val audioRecordManager: AudioRecordManager,
    private val recordFileResp: RecordingFileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaHomeUiState())
    val uiState = _uiState.asStateFlow()
    private var recordTimingJob: Job? = null

    init {
        Logger.d(TAG, "init.")
        viewModelScope.launch {
            audioRecordManager.isRecording.collect {
                reduce { copy(isRecord = it) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared.")
        audioRecordManager.stop()
        recordTimingJob?.cancel()
        recordTimingJob = null
    }

    fun handleUiIntent(intent: MediaHomeUiIntent) {
        Logger.d(TAG, "handleUiIntent: $intent")
        when (intent) {
            is MediaHomeUiIntent.SwitchRecord -> switchRecord(intent.isRecord)
        }
    }

    private fun switchRecord(isRecord: Boolean) {
        if (isRecord) {
            audioRecordManager.start(true)
        } else {
            audioRecordManager.stop()
        }
        recordTiming(isRecord)
    }

    private fun recordTiming(isRecord: Boolean) {
        recordTimingJob?.cancel()
        if (isRecord) {
            recordTimingJob = viewModelScope.launch {
                while (true) {
                    delay(1000.milliseconds)
                    reduce { copy(recordTimes = recordTimes + 1000) }
                }
            }
        } else {
            reduce { copy(recordTimes = 0L) }
            recordTimingJob = null
        }
    }

    private fun reduce(reducer: MediaHomeUiState.() -> MediaHomeUiState) {
        _uiState.update(reducer)
    }

    companion object {
        const val TAG = "MediaViewModel"
    }
}

data class MediaHomeUiState(
    val isRecord: Boolean = false,
    val recordTimes: Long = 0L,
)

sealed interface MediaHomeUiIntent {
    data class SwitchRecord(val isRecord: Boolean) : MediaHomeUiIntent
}