package com.lunacattus.conflux.ui.sections.llm.tts

import com.lunacattus.conflux.domain.tts.TtsConfig

enum class RequestMode { HTTP, WebSocket }

data class Speaker(
    val name: String,
    val description: String,
    val nativeLanguage: String,
) {
    companion object {
        val ALL = listOf(
            Speaker("Vivian", "明亮、略带锐气的年轻女声", "中文"),
            Speaker("Serena", "温暖柔和的年轻女声", "中文"),
            Speaker("Uncle_Fu", "音色低沉醇厚的成熟男声", "中文"),
            Speaker("Dylan", "清晰自然的北京青年男声", "中文（北京方言）"),
            Speaker("Eric", "活泼、略带沙哑明亮感的成都男声", "中文（四川方言）"),
            Speaker("Ryan", "富有节奏感的动态男声", "英语"),
            Speaker("Aiden", "清晰中频的阳光美式男声", "英语"),
            Speaker("Ono_Anna", "轻快灵活的俏皮日语女声", "日语"),
            Speaker("Sohee", "富含情感的温暖韩语女声", "韩语"),
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
