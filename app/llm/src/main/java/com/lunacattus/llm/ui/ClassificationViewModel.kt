package com.lunacattus.llm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.common.coroutine.SafeCoroutine.launchSafe
import com.lunacattus.llm.di.LLAMA
import com.lunacattus.llm.di.ONNX
import com.lunacattus.llm.domain.base.IBertLlm
import com.lunacattus.llm.domain.local.OnnxLlmRepository
import com.lunacattus.llm.model.ClassificationUiState
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
class ClassificationViewModel @Inject constructor(
    @param:LLAMA private val bertLlm: IBertLlm,
    @param:ONNX private val onnxLlm: IBertLlm,
) : ViewModel() {

    private val _state = MutableStateFlow(ClassificationUiState())
    val state = _state.asStateFlow()

    companion object {
        const val TAG = "ClassificationViewModel"

        private val LABELS = mapOf(
            0 to "出行",
            1 to "多意图",
            2 to "影音",
            3 to "播控",
            4 to "电话",
            5 to "直接车控",
            6 to "车书",
            7 to "闲聊"
        )
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
        bertLlm.unLoad()
        bertLlm.shutDown()
        onnxLlm.unLoad()
        onnxLlm.shutDown()
        super.onCleared()
    }

    // ── BERT (llama.cpp JNI) ──

    fun initBertModel(modelPath: String) {
        Logger.d(TAG, "initBertModel: $modelPath")
        _state.update { it.copy(bertModelPath = modelPath) }
        viewModelScope.launchSafe("initBertModel") {
            bertLlm.init(modelPath).onStart {
                _state.update { it.copy(bertState = ModelState.Loading) }
            }.collect { result ->
                result.onSuccess { ok ->
                    if (ok) _state.update { it.copy(bertState = ModelState.Loaded) }
                    else _state.update { it.copy(bertState = ModelState.Error("init failed")) }
                }.onFailure { thr ->
                    _state.update {
                        it.copy(bertState = ModelState.Error(thr.message ?: "unknown"))
                    }
                }
            }
        }
    }

    fun classifyBert(text: String) {
        val startTime = System.currentTimeMillis()
        _state.update { it.copy(bertTimeMs = null) }
        bertLlm.classify(text).onEach { result ->
            result.onSuccess { idx ->
                val label = LABELS[idx] ?: "未知"
                Logger.d(TAG, "BERT classify: class=$idx ($label)")
                _state.update { it.copy(bertResult = idx, bertLabel = label) }
            }.onFailure { thr ->
                Logger.e(TAG, "BERT classify failed: ${thr.message}")
                _state.update { it.copy(bertLabel = "分类失败") }
            }
        }.onCompletion {
            val elapsed = System.currentTimeMillis() - startTime
            _state.update { it.copy(bertTimeMs = elapsed) }
            Logger.d(TAG, "BERT classify completed in ${elapsed}ms")
        }.launchIn(viewModelScope)
    }

    // ── ONNX Runtime ──

    fun initOnnxModel(modelPath: String) {
        Logger.d(TAG, "initOnnxModel: $modelPath")
        _state.update { it.copy(onnxModelPath = modelPath) }
        viewModelScope.launchSafe("initOnnxModel") {
            (onnxLlm as? OnnxLlmRepository)?.useNpu = _state.value.onnxUseNpu
            onnxLlm.init(modelPath).onStart {
                _state.update { it.copy(onnxState = ModelState.Loading) }
            }.collect { result ->
                result.onSuccess { ok ->
                    if (ok) _state.update { it.copy(onnxState = ModelState.Loaded) }
                    else _state.update { it.copy(onnxState = ModelState.Error("init failed")) }
                }.onFailure { thr ->
                    _state.update {
                        it.copy(onnxState = ModelState.Error(thr.message ?: "unknown"))
                    }
                }
            }
        }
    }

    fun classifyOnnx(text: String) {
        val startTime = System.currentTimeMillis()
        _state.update { it.copy(onnxTimeMs = null) }
        onnxLlm.classify(text).onEach { result ->
            result.onSuccess { idx ->
                val label = LABELS[idx] ?: "未知"
                Logger.d(TAG, "ONNX classify: class=$idx ($label)")
                _state.update { it.copy(onnxResult = idx, onnxLabel = label) }
            }.onFailure { thr ->
                Logger.e(TAG, "ONNX classify failed: ${thr.message}")
                _state.update { it.copy(onnxLabel = "分类失败") }
            }
        }.onCompletion {
            val elapsed = System.currentTimeMillis() - startTime
            _state.update { it.copy(onnxTimeMs = elapsed) }
            Logger.d(TAG, "ONNX classify completed in ${elapsed}ms")
        }.launchIn(viewModelScope)
    }

    fun toggleNpu(enabled: Boolean) {
        Logger.d(TAG, "toggleNpu: $enabled")
        _state.update { it.copy(onnxUseNpu = enabled) }
        val path = _state.value.onnxModelPath
        if (path.isNotEmpty()) {
            initOnnxModel(path)
        }
    }
}
