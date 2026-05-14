package com.lunacattus.conflux.ui.sections.llm.tts

import androidx.annotation.StringRes
import com.lunacattus.conflux.R
import com.lunacattus.conflux.domain.tts.TtsConfig

enum class RequestMode { HTTP, WebSocket }

data class Speaker(
    val name: String,
    @StringRes val descriptionRes: Int,
    @StringRes val nativeLanguageRes: Int,
) {
    companion object {
        val ALL = listOf(
            Speaker("Vivian", R.string.tts_speaker_vivian_desc, R.string.tts_speaker_vivian_lang),
            Speaker("Serena", R.string.tts_speaker_serena_desc, R.string.tts_speaker_serena_lang),
            Speaker("Uncle_Fu", R.string.tts_speaker_uncle_fu_desc, R.string.tts_speaker_uncle_fu_lang),
            Speaker("Dylan", R.string.tts_speaker_dylan_desc, R.string.tts_speaker_dylan_lang),
            Speaker("Eric", R.string.tts_speaker_eric_desc, R.string.tts_speaker_eric_lang),
            Speaker("Ryan", R.string.tts_speaker_ryan_desc, R.string.tts_speaker_ryan_lang),
            Speaker("Aiden", R.string.tts_speaker_aiden_desc, R.string.tts_speaker_aiden_lang),
            Speaker("Ono_Anna", R.string.tts_speaker_ono_anna_desc, R.string.tts_speaker_ono_anna_lang),
            Speaker("Sohee", R.string.tts_speaker_sohee_desc, R.string.tts_speaker_sohee_lang),
        )
        val DEFAULT = ALL[0]
    }
}

val LANGUAGES = listOf(
    "Chinese", "English", "Japanese", "Korean", "German",
    "French", "Russian", "Portuguese", "Spanish", "Italian",
)

data class TtsState(
    val host: String = TtsConfig.DEFAULT_HOST,
    val port: String = TtsConfig.DEFAULT_PORT.toString(),
    val speaker: Speaker = Speaker.DEFAULT,
    val language: String = "Chinese",
    val instruct: String = "",
    val requestMode: RequestMode = RequestMode.WebSocket,
    val wsConnected: Boolean = false,
    val wsConnecting: Boolean = false,
    val httpRequesting: Boolean = false,
    val inputText: String = "",
    val longText: String = "",
    val longTextFilename: String? = null,
    val messageGroups: List<TtsMessageGroup> = emptyList(),
    val playingGroupId: String? = null,
    val playbackSpeed: Float = 1.0f,
    val playbackVolume: Float = 1.0f,
    val error: String? = null,
)

data class TtsMessageGroup(
    val id: String,
    val requestId: String = "",
    val text: String,
    val timestamp: Long,
    val speaker: String = Speaker.DEFAULT.name,
    val language: String = "Chinese",
    val chunks: List<TtsAudioChunk> = emptyList(),
    val isCompleted: Boolean = false,
    val totalChunks: Int = 0,
    val mode: RequestMode = RequestMode.WebSocket,
    val isLongText: Boolean = false,
)

data class TtsAudioChunk(
    val index: Int,
    val audioB64: String,
    val durationMs: Long,
)

sealed interface TtsIntent {
    data class UpdateHost(val host: String) : TtsIntent
    data class UpdatePort(val port: String) : TtsIntent
    data class SelectSpeaker(val speaker: Speaker) : TtsIntent
    data class SelectLanguage(val language: String) : TtsIntent
    data class UpdateInstruct(val instruct: String) : TtsIntent
    data class SetRequestMode(val mode: RequestMode) : TtsIntent
    data class UpdateText(val text: String) : TtsIntent
    data class UpdateLongText(val text: String) : TtsIntent
    data class SetLongTextFromFile(val text: String, val filename: String) : TtsIntent
    data object SendText : TtsIntent
    data class PlayGroup(val groupId: String) : TtsIntent
    data class PlaySingleChunk(val groupId: String, val chunkIndex: Int) : TtsIntent
    data object StopAudio : TtsIntent
    data class SetPlaybackSpeed(val speed: Float) : TtsIntent
    data class SetPlaybackVolume(val volume: Float) : TtsIntent
    data class DeleteGroup(val groupId: String) : TtsIntent
    data object ClearAll : TtsIntent
    data object ConnectWs : TtsIntent
    data object DisconnectWs : TtsIntent
    data object HealthCheck : TtsIntent
    data object DismissError : TtsIntent
}

sealed interface TtsEffect {
    data class ShowToast(val message: String) : TtsEffect
    data object ScrollToBottom : TtsEffect
}
