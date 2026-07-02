package com.lunacattus.llm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.common.coroutine.SafeCoroutine.launchSafe
import com.lunacattus.llm.domain.base.IGenerateLlm
import com.lunacattus.llm.model.GenerativeUiState
import com.lunacattus.llm.model.ModelState
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GenerativeViewModel @Inject constructor(
    private val generateLlm: IGenerateLlm,
) : ViewModel() {

    private val _state = MutableStateFlow(GenerativeUiState())
    val state = _state.asStateFlow()

    companion object {
        const val TAG = "GenerativeViewModel"
        const val DEFAULT_PREDICT_LENGTH = 128

        private const val SYSTEM_PROMPT = "你扮演邻家大姐姐的角色，说话超级温柔，带上文字表情和语气词"
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
        generateLlm.unLoad()
        generateLlm.shutDown()
        super.onCleared()
    }

    fun initModel(modelPath: String) {
        Logger.d(TAG, "initModel: $modelPath")
        _state.update { it.copy(generateModelPath = modelPath) }
        viewModelScope.launchSafe("initModel") {
            generateLlm.init(modelPath).onStart {
                _state.update { it.copy(generateState = ModelState.Loading) }
            }.collect { result ->
                result.onSuccess { ok ->
                    if (ok) {
                        _state.update { it.copy(generateState = ModelState.Loaded) }
                        sendSystemPrompt(SYSTEM_PROMPT)
                    } else {
                        _state.update { it.copy(generateState = ModelState.Error("init failed")) }
                    }
                }.onFailure { thr ->
                    _state.update {
                        it.copy(generateState = ModelState.Error(thr.message ?: "unknown"))
                    }
                }
            }
        }
    }

    fun sendUserPrompt(prompt: String, predictLength: Int = DEFAULT_PREDICT_LENGTH) {
        val startTime = System.currentTimeMillis()
        _state.update { it.copy(assistantResponse = "", responseTimeMs = null) }
        generateLlm.sendUserPrompt(prompt, predictLength).onEach { result ->
            result.onSuccess { token ->
                if (token.isNotEmpty()) {
                    _state.update { s -> s.copy(assistantResponse = s.assistantResponse + token) }
                }
            }
        }.onCompletion {
            val elapsed = System.currentTimeMillis() - startTime
            _state.update { s -> s.copy(responseTimeMs = elapsed) }
            Logger.d(TAG, "sendUserPrompt completed in ${elapsed}ms")
        }.launchIn(viewModelScope)
    }

    private fun sendSystemPrompt(prompt: String) {
        generateLlm.sendSystemPrompt(prompt).onEach { result ->
            Logger.d(TAG, "sendSystemPrompt: $result")
            result.onSuccess { ok ->
                _state.update { s -> s.copy(systemPromptReady = ok) }
            }
        }.launchIn(viewModelScope)
    }
}
