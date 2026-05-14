package com.lunacattus.conflux.ui.sections.llm.tts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lunacattus.conflux.domain.tts.TtsAudioPlayer
import com.lunacattus.conflux.domain.tts.TtsConfig
import com.lunacattus.conflux.domain.tts.TtsHttpService
import com.lunacattus.conflux.domain.tts.TtsWebSocketService
import com.lunacattus.conflux.domain.tts.TtsWsEvent
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TtsViewModel @Inject constructor(
    private val httpService: TtsHttpService,
    private val wsService: TtsWebSocketService,
    private val audioPlayer: TtsAudioPlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(TtsState())
    val state: StateFlow<TtsState> = _state.asStateFlow()

    private val _effects = Channel<TtsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var pendingLongTextGroups = mutableListOf<String>()
    private val gson = Gson()

    private val metadataFile: File
        get() = File(audioPlayer.audioDir, "groups_metadata.json")

    init {
        loadPersistedGroups()
        observeWsEvents()
        observeWsState()
        observePlayerState()
    }

    fun handleIntent(intent: TtsIntent) {
        when (intent) {
            is TtsIntent.UpdateHost -> { reduce { copy(host = intent.host) }; applyConfig() }
            is TtsIntent.UpdatePort -> { reduce { copy(port = intent.port) }; applyConfig() }
            is TtsIntent.SelectSpeaker -> reduce { copy(speaker = intent.speaker) }
            is TtsIntent.SelectLanguage -> reduce { copy(language = intent.language) }
            is TtsIntent.UpdateInstruct -> reduce { copy(instruct = intent.instruct) }
            is TtsIntent.SetRequestMode -> reduce { copy(requestMode = intent.mode) }
            is TtsIntent.UpdateText -> reduce { copy(inputText = intent.text) }
            is TtsIntent.UpdateLongText -> reduce { copy(longText = intent.text) }
            is TtsIntent.SetLongTextFromFile -> reduce { copy(longText = intent.text, longTextFilename = intent.filename) }
            is TtsIntent.SendText -> sendText()
            is TtsIntent.PlayGroup -> playGroup(intent.groupId)
            is TtsIntent.PlaySingleChunk -> playSingleChunk(intent.groupId, intent.chunkIndex)
            is TtsIntent.StopAudio -> { audioPlayer.stop(); reduce { copy(playingGroupId = null) } }
            is TtsIntent.SetPlaybackSpeed -> { audioPlayer.setSpeed(intent.speed); reduce { copy(playbackSpeed = intent.speed) } }
            is TtsIntent.SetPlaybackVolume -> { audioPlayer.setVolume(intent.volume); reduce { copy(playbackVolume = intent.volume) } }
            is TtsIntent.DeleteGroup -> deleteGroup(intent.groupId)
            is TtsIntent.ClearAll -> clearAll()
            is TtsIntent.ConnectWs -> connectWs()
            is TtsIntent.DisconnectWs -> disconnectWs()
            is TtsIntent.HealthCheck -> healthCheck()
            is TtsIntent.DismissError -> reduce { copy(error = null) }
        }
    }

    private fun applyConfig() {
        val s = _state.value
        val port = s.port.toIntOrNull() ?: TtsConfig.DEFAULT_PORT
        val config = TtsConfig(host = s.host, port = port)
        httpService.updateConfig(config)
        wsService.updateConfig(config)
    }

    private fun sendText() {
        val state = _state.value
        val shortText = state.inputText.trim()
        var isLong = false

        val finalText = when {
            shortText.isNotEmpty() -> { reduce { copy(inputText = "") }; shortText }
            state.longText.isNotBlank() -> { isLong = true; state.longText.trim() }
            else -> return
        }

        val texts = if (isLong) chunkLongText(finalText) else listOf(finalText)

        pendingLongTextGroups.clear()
        if (isLong) {
            pendingLongTextGroups.addAll(texts.indices.map { i -> "group_${System.nanoTime()}_$i" })
        }

        reduce { copy(error = null, longText = "", longTextFilename = null) }

        when (state.requestMode) {
            RequestMode.HTTP -> sendHttp(texts, isLong)
            RequestMode.WebSocket -> sendWs(texts, isLong)
        }
    }

    private fun chunkLongText(text: String, chunkSize: Int = 200): List<String> {
        val chunks = mutableListOf<String>()
        var i = 0
        while (i < text.length) {
            val end = minOf(i + chunkSize, text.length)
            chunks.add(text.substring(i, end))
            i = end
        }
        return chunks
    }

    private fun sendHttp(texts: List<String>, isLong: Boolean) {
        reduce { copy(httpRequesting = true) }
        val state = _state.value
        val now = System.currentTimeMillis()
        val totalGroups = texts.size

        val groupIds = texts.mapIndexed { index, text ->
            val groupId = if (isLong && index < pendingLongTextGroups.size) {
                pendingLongTextGroups[index]
            } else {
                "group_${System.currentTimeMillis()}_$index"
            }
            val displayText = if (isLong) "[${index + 1}/$totalGroups] $text" else text
            val group = TtsMessageGroup(
                id = groupId, text = displayText, timestamp = now + index,
                speaker = state.speaker.name, language = state.language,
                mode = RequestMode.HTTP, isLongText = isLong,
            )
            reduce { copy(messageGroups = messageGroups + group) }
            groupId to group
        }
        persistGroups()

        viewModelScope.launch {
            var failed = false
            for ((index, pair) in groupIds.withIndex()) {
                if (failed) break
                val (groupId, _) = pair
                val result = httpService.synthesize(texts[index], state.speaker.name, state.language, state.instruct.ifBlank { null })
                result.onSuccess { response ->
                    val chunks = response.chunks.map { c -> TtsAudioChunk(c.index, c.audioB64, c.durationMs) }
                    updateGroup(groupId) { copy(chunks = chunks, totalChunks = chunks.size, isCompleted = true) }
                    persistGroups()
                }.onFailure { e ->
                    Logger.e(TAG, "HTTP TTS failed: ${e.message}")
                    if (isLong) updateGroup(groupId) { copy(isCompleted = true) } else removeGroup(groupId)
                    persistGroups()
                    reduce { copy(error = "HTTP 请求失败: ${e.message}", httpRequesting = false) }
                    emitEffect(TtsEffect.ShowToast("HTTP 请求失败"))
                    failed = true
                }
            }
            if (!failed) reduce { copy(httpRequesting = false) }
            emitEffect(TtsEffect.ScrollToBottom)
        }
    }

    private fun sendWs(texts: List<String>, isLong: Boolean) {
        val state = _state.value
        viewModelScope.launch {
            var failed = false
            for (text in texts) {
                if (failed) break
                val requestId = wsService.sendText(text, state.speaker.name, state.language, state.instruct.ifBlank { null })
                if (requestId == null) {
                    reduce { copy(error = "WebSocket 发送失败") }
                    emitEffect(TtsEffect.ShowToast("发送失败"))
                    failed = true
                }
            }
        }
    }

    private fun observeWsEvents() {
        viewModelScope.launch {
            wsService.events.collect { event ->
                when (event) {
                    is TtsWsEvent.ChunkStart -> handleChunkStart(event)
                    is TtsWsEvent.Audio -> handleAudioChunk(event)
                    is TtsWsEvent.Done -> handleWsDone(event)
                    is TtsWsEvent.Error -> handleWsError(event)
                    is TtsWsEvent.SessionId -> Logger.d(TAG, "Session: ${event.sessionId}")
                    is TtsWsEvent.Pong -> Logger.d(TAG, "Pong")
                }
            }
        }
    }

    private suspend fun handleChunkStart(event: TtsWsEvent.ChunkStart) {
        if (_state.value.messageGroups.any { it.id == event.requestId }) return
        val state = _state.value
        val group = TtsMessageGroup(id = event.requestId, text = event.text, timestamp = System.currentTimeMillis(),
            speaker = state.speaker.name, language = state.language, totalChunks = event.total, mode = RequestMode.WebSocket)
        reduce { copy(messageGroups = sortGroups(messageGroups + group)) }
        persistGroups()
        emitEffect(TtsEffect.ScrollToBottom)
    }

    private suspend fun handleAudioChunk(event: TtsWsEvent.Audio) {
        updateGroup(event.requestId) { copy(chunks = (chunks + TtsAudioChunk(event.chunkIndex, event.audioB64, event.durationMs)).sortedBy { it.index }) }
        persistGroups()
    }

    private suspend fun handleWsDone(event: TtsWsEvent.Done) {
        updateGroup(event.requestId) { copy(isCompleted = true) }
        persistGroups()
    }

    private suspend fun handleWsError(event: TtsWsEvent.Error) {
        updateGroup(event.requestId) { copy(isCompleted = true) }
        persistGroups()
        reduce { copy(error = "WS 错误 [${event.requestId}]: ${event.message}") }
        emitEffect(TtsEffect.ShowToast("WS 错误: ${event.message}"))
    }

    private fun observeWsState() {
        viewModelScope.launch {
            wsService.connected.collect { reduce { copy(wsConnected = it) } }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            audioPlayer.playingGroupId.collect { if (it == null) reduce { copy(playingGroupId = null) } }
        }
    }

    private fun playGroup(groupId: String) {
        val group = _state.value.messageGroups.find { it.id == groupId } ?: return
        val b64List = group.chunks.map { it.audioB64 }
        if (b64List.isEmpty()) return
        audioPlayer.playAllChunks(b64List, groupId)
        reduce { copy(playingGroupId = groupId, playbackSpeed = audioPlayer.playbackSpeed.value, playbackVolume = audioPlayer.playbackVolume.value) }
    }

    private fun playSingleChunk(groupId: String, chunkIndex: Int) {
        val group = _state.value.messageGroups.find { it.id == groupId } ?: return
        val chunk = group.chunks.getOrNull(chunkIndex) ?: return
        audioPlayer.playSingleChunk(chunk.audioB64, groupId)
        reduce { copy(playingGroupId = groupId, playbackSpeed = audioPlayer.playbackSpeed.value, playbackVolume = audioPlayer.playbackVolume.value) }
    }

    private fun deleteGroup(groupId: String) {
        audioPlayer.deleteGroupFiles(groupId)
        removeGroup(groupId)
        persistGroups()
    }

    private fun clearAll() {
        audioPlayer.clearAllFiles()
        reduce { copy(messageGroups = emptyList()) }
        metadataFile.delete()
    }

    private fun connectWs() {
        viewModelScope.launch { reduce { copy(wsConnecting = true) }; wsService.connect(); reduce { copy(wsConnecting = false) } }
    }

    private fun disconnectWs() {
        viewModelScope.launch { wsService.disconnect() }
    }

    private fun healthCheck() {
        viewModelScope.launch {
            httpService.healthCheck()
                .onSuccess { emitEffect(TtsEffect.ShowToast("${it.status} | 模型:${it.modelLoaded} | 活跃:${it.activeSessions}")) }
                .onFailure { emitEffect(TtsEffect.ShowToast("健康检查失败: ${it.message}")) }
        }
    }

    private fun updateGroup(groupId: String, updater: TtsMessageGroup.() -> TtsMessageGroup) {
        reduce { copy(messageGroups = messageGroups.map { if (it.id == groupId) it.updater() else it }) }
    }

    private fun removeGroup(groupId: String) {
        reduce { copy(messageGroups = messageGroups.filter { it.id != groupId }) }
    }

    private fun sortGroups(groups: List<TtsMessageGroup>): List<TtsMessageGroup> = groups.sortedBy { it.timestamp }

    private fun reduce(reducer: TtsState.() -> TtsState) { _state.update(reducer) }

    private fun emitEffect(effect: TtsEffect) { viewModelScope.launch { _effects.send(effect) } }

    private fun persistGroups() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = gson.toJson(_state.value.messageGroups)
                metadataFile.writeText(data)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to persist groups: ${e.message}")
            }
        }
    }

    private fun loadPersistedGroups() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!metadataFile.exists()) return@launch
                val json = metadataFile.readText()
                val type = object : TypeToken<List<TtsMessageGroup>>() {}.type
                val groups: List<TtsMessageGroup> = gson.fromJson(json, type)
                withContext(Dispatchers.Main) { reduce { copy(messageGroups = groups) } }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load persisted groups: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }

    companion object {
        private const val TAG = "TtsViewModel"
    }
}
