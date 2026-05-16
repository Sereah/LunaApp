package com.lunacattus.conflux.ui.sections.llm.polish

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lunacattus.conflux.domain.llm.polish.PolishConfig
import com.lunacattus.conflux.domain.llm.polish.PolishHttpService
import com.lunacattus.conflux.ui.ActivityToastEvent
import com.lunacattus.conflux.ui.ToastEvent
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PolishViewModel @Inject constructor(
    private val httpService: PolishHttpService,
    private val application: Application,
) : ViewModel() {

    private val _state = MutableStateFlow(PolishState())
    val state: StateFlow<PolishState> = _state.asStateFlow()

    private val _effects = Channel<PolishEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val gson = Gson()

    private val metadataFile: File
        get() = File(application.filesDir, "polish_results.json")

    init {
        loadPersistedResults()
    }

    fun handleIntent(intent: PolishIntent) {
        when (intent) {
            is PolishIntent.UpdateHost -> { reduce { copy(host = intent.host) }; applyConfig() }
            is PolishIntent.UpdatePort -> { reduce { copy(port = intent.port) }; applyConfig() }
            is PolishIntent.UpdateText -> reduce { copy(inputText = intent.text) }
            is PolishIntent.UpdateTemperature -> reduce { copy(temperature = intent.temperature) }
            is PolishIntent.SelectPreset -> reduce { copy(inputText = intent.text) }
            is PolishIntent.SendPolish -> sendPolish()
            is PolishIntent.DeleteResult -> deleteResult(intent.id)
            is PolishIntent.ClearAll -> clearAll()
        }
    }

    private fun applyConfig() {
        val s = _state.value
        val port = s.port.toIntOrNull() ?: PolishConfig.DEFAULT_PORT
        val config = PolishConfig(host = s.host.ifBlank { PolishConfig.DEFAULT_HOST }, port = port)
        httpService.updateConfig(config)
    }

    private fun sendPolish() {
        val state = _state.value
        val text = state.inputText.trim()
        if (text.isEmpty()) return

        reduce { copy(isRequesting = true, inputText = "") }

        viewModelScope.launch {
            val result = httpService.polish(text, state.temperature)
            result.onSuccess { response ->
                val item = PolishResultItem(
                    id = UUID.randomUUID().toString(),
                    requestId = response.requestId,
                    originalText = text,
                    polishedText = response.txt,
                    temperature = state.temperature,
                    timestamp = System.currentTimeMillis(),
                )
                reduce { copy(results = results + item, isRequesting = false) }
                persistResults()
                emitEffect(PolishEffect.ScrollToBottom)
            }.onFailure { e ->
                Logger.e(TAG, "Polish request failed: ${e.message}")
                reduce { copy(isRequesting = false) }
                ActivityToastEvent.send(ToastEvent.ShowToast("请求失败: ${e.message}"))
            }
        }
    }

    private fun deleteResult(id: String) {
        reduce { copy(results = results.filter { it.id != id }) }
        persistResults()
    }

    private fun clearAll() {
        reduce { copy(results = emptyList()) }
        metadataFile.delete()
    }

    private fun reduce(reducer: PolishState.() -> PolishState) {
        _state.update(reducer)
    }

    private fun emitEffect(effect: PolishEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun persistResults() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = gson.toJson(_state.value.results)
                metadataFile.writeText(data)
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to persist results: ${e.message}")
            }
        }
    }

    private fun loadPersistedResults() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!metadataFile.exists()) return@launch
                val json = metadataFile.readText()
                val type = object : TypeToken<List<PolishResultItem>>() {}.type
                val items: List<PolishResultItem> = gson.fromJson(json, type)
                withContext(Dispatchers.Main) { reduce { copy(results = items) } }
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load persisted results: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "PolishViewModel"
    }
}
