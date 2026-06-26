package com.lunacattus.llm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.common.coroutine.SafeCoroutine.launchSafe
import com.lunacattus.llm.domain.base.IBertLlm
import com.lunacattus.llm.domain.base.IGenerateLlm
import com.lunacattus.llm.model.ModelState
import com.lunacattus.llm.model.UiState
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
class MainViewModel @Inject constructor(
    private val generateLlm: IGenerateLlm,
    private val bertLlm: IBertLlm
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    companion object {
        const val TAG = "MainViewModel"
        const val DEFAULT_PREDICT_LENGTH = 128

        private const val SYSTEM_PROMPT = "你扮演邻家大姐姐的角色，说话超级温柔，带上文字表情和语气词"

        private val LABELS = mapOf(
            0 to "出行",
            1 to "影音",
            2 to "感知车控",
            3 to "搜索",
            4 to "直接车控",
            5 to "车书",
            6 to "闲聊"
        )
    }

    init {
        Logger.d(TAG, "init")
        initGenerateLlm()
        initBertLLm()
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
        generateLlm.unLoad()
        generateLlm.shutDown()
        bertLlm.unLoad()
        bertLlm.shutDown()
        super.onCleared()
    }

    fun sendSystemPrompt(prompt: String) {
        generateLlm.sendSystemPrompt(prompt).onEach {
            Logger.d(TAG, "sendSystemPrompt: $it")
            it.onSuccess { ok ->
                _state.update { s -> s.copy(systemPromptReady = ok) }
            }
        }.launchIn(viewModelScope)
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
            Logger.d(TAG, "sendUserPrompt token: $result")
        }.onCompletion {
            val elapsed = System.currentTimeMillis() - startTime
            _state.update { s -> s.copy(responseTimeMs = elapsed) }
            Logger.d(TAG, "sendUserPrompt completed in ${elapsed}ms")
        }.launchIn(viewModelScope)
    }

    fun classify(text: String) {
        val startTime = System.currentTimeMillis()
        _state.update { it.copy(classificationTimeMs = null) }
        bertLlm.classify(text).onEach { result ->
            result.onSuccess { idx ->
                val label = LABELS[idx] ?: "未知"
                Logger.d(TAG, "classify result: class=$idx ($label)")
                _state.update { s ->
                    s.copy(
                        classificationResult = idx,
                        classificationLabel = label
                    )
                }
            }.onFailure { thr ->
                Logger.e(TAG, "classify failed: ${thr.message}")
                _state.update { s -> s.copy(classificationLabel = "分类失败") }
            }
        }.onCompletion {
            val elapsed = System.currentTimeMillis() - startTime
            _state.update { s -> s.copy(classificationTimeMs = elapsed) }
            Logger.d(TAG, "classify completed in ${elapsed}ms")
        }.launchIn(viewModelScope)
    }

    private fun initGenerateLlm() {
        viewModelScope.launchSafe("initGenerateLlm") {
            generateLlm.init().onStart {
                _state.update { it.copy(generateState = ModelState.Loading) }
            }.collect { result ->
                result.onSuccess { isSuccess ->
                    if (isSuccess) {
                        _state.update { it.copy(generateState = ModelState.Loaded) }
                        sendSystemPrompt(SYSTEM_PROMPT)
                    } else {
                        _state.update { it.copy(generateState = ModelState.Error("init failed")) }
                    }
                }.onFailure { thr ->
                    _state.update {
                        it.copy(
                            generateState = ModelState.Error(
                                thr.message ?: "unknown"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun initBertLLm() {
        viewModelScope.launchSafe("initBertLLm") {
            bertLlm.init().onStart {
                _state.update { it.copy(bertState = ModelState.Loading) }
            }.collect { result ->
                result.onSuccess { isSuccess ->
                    if (isSuccess) {
                        _state.update { it.copy(bertState = ModelState.Loaded) }
                    } else {
                        _state.update { it.copy(bertState = ModelState.Error("init failed")) }
                    }
                }.onFailure { thr ->
                    _state.update {
                        it.copy(bertState = ModelState.Error(thr.message ?: "unknown"))
                    }
                }
            }
        }
    }
}