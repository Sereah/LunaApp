package com.lunacattus.conflux.ui.sections.media.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.conflux.ui.ActivityToastEvent
import com.lunacattus.conflux.ui.ToastEvent
import com.lunacattus.logger.Logger
import com.lunacattus.voice.AuthInfo
import com.lunacattus.voice.Voice
import com.lunacattus.voice.record.AudioRecordManager
import com.lunacattus.voice.record.RecordingFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val voice: Voice,
    private val audioRecordManager: AudioRecordManager,
    private val recordFileResp: RecordingFileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Logger.d(TAG, "init.")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared.")
    }

    fun handleUiIntent(intent: MediaHomeUiIntent) {
        Logger.d(TAG, "handleUiIntent: $intent")
        when (intent) {
            MediaHomeUiIntent.InitVoiceBasic -> initVoiceBasic()
            is MediaHomeUiIntent.SwitchRecord -> switchRecord(intent.isRecord)
            MediaHomeUiIntent.OpenRecordingFile -> openRecordingFile()
        }
    }

    private fun initVoiceBasic() {
        viewModelScope.launch {
            reduce { copy(voiceBasicInitState = VoiceBasicState.Authing) }
            voice.init(authInfo).collect { authResult ->
                reduce {
                    copy(
                        voiceBasicInitState = if (authResult.success) {
                            VoiceBasicState.Authed
                        } else {
                            VoiceBasicState.UnAuth
                        }
                    )
                }
                ActivityToastEvent.send(
                    ToastEvent.ShowToast(
                        if (authResult.success) {
                            "语音初始化成功"
                        } else {
                            "语音初始化失败: ${authResult.msg}"
                        }
                    )
                )
            }
        }
    }

    private fun switchRecord(isRecord: Boolean) {
        if (isRecord) {
            audioRecordManager.start(true)
        } else {
            audioRecordManager.stop()
        }
        viewModelScope.launch {
            audioRecordManager.isRecording.collect {
                reduce { copy(isRecord = it) }
            }
        }
    }

    private fun openRecordingFile() {
        recordFileResp.getWavFiles().let {
            if (it.isNotEmpty()) {
                recordFileResp.playWithSystemPlayer(it[0])
            }
        }
    }

    private fun reduce(reducer: MediaHomeUiState.() -> MediaHomeUiState) {
        _uiState.update(reducer)
    }

    companion object {
        const val TAG = "MediaViewModel"
        private val authInfo = AuthInfo(
            apiKey = "540c772fd3d3540c772fd3d3695bcbc4",
            productId = "279633473",
            productKey = "11060f9232830d0de1f7225c33cf93df",
            productSecret = "a42fad2312eba7d1e41379ec6a6d337f"
        )
    }
}

data class MediaHomeUiState(
    val voiceBasicInitState: VoiceBasicState = VoiceBasicState.UnAuth,
    val isRecord: Boolean = false
)

sealed interface MediaHomeUiIntent {
    data object InitVoiceBasic : MediaHomeUiIntent
    data class SwitchRecord(val isRecord: Boolean) : MediaHomeUiIntent
    data object OpenRecordingFile: MediaHomeUiIntent
}

enum class VoiceBasicState {
    UnAuth, Authing, Authed
}