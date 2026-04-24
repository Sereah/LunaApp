package com.lunacattus.conflux.ui.sections.home.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunacattus.conflux.di.Gemma
import com.lunacattus.conflux.domain.llm.ILLMManager
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:Gemma private val llmManager: ILLMManager
) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }

    init {
        Logger.d(TAG, "init: 准备加载 270M 模型...")
        viewModelScope.launch {
            val isSuccess = llmManager.initModel()
            if (isSuccess) {
                Logger.d(TAG, "模型加载成功 🚀！马上开始测试生成...")
            } else {
                Logger.e(TAG, "模型加载失败 ❌，请检查 assets 拷贝逻辑或设备支持情况。")
            }
        }
    }

    fun runHardcodedTest() {
        val testInput = "打电话给老婆"
        Logger.d(TAG, "========== 开始 NLU 推理 ==========")
        Logger.d(TAG, "用户输入: $testInput")

        viewModelScope.launch {
            val resultBuilder = StringBuilder()

            llmManager.generate(testInput)
                .catch { e ->
                    Logger.e(TAG, "推理崩溃了: ${e.message}")
                }
                .onCompletion { error ->
                    if (error == null) {
                        val finalResult = resultBuilder.toString()
                        Logger.d(TAG, "========== 推理结束 ==========")
                        Logger.d(TAG, "🎉 最终模型输出的完整 JSON 为:\n$finalResult")
                    }
                }
                .collect { chunk ->
                    resultBuilder.append(chunk)
                    Logger.d(TAG, "吐字: $chunk")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared: 释放 LiteRT 引擎资源")
        llmManager.release()
    }
}