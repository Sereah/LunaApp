package com.lunacattus.llm.domain.local

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import com.lunacattus.llm.domain.base.IBertLlm
import com.lunacattus.llm.model.BertException
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

/**
 * 基于 ONNX Runtime 的 NLU 意图分类仓库。
 *
 * 与 [BertLLmRepository] 实现相同接口，但使用 ONNX Runtime Java API
 * 替代 llama.cpp JNI。模型需先经 convert_to_onnx.py 转换为
 * 内置 tokenizer + 后处理的 nlu_model_mobile.onnx。
 *
 * 用法：
 *   repo.init("/data/.../nlu_model_mobile.onnx")  // 加载模型
 *   repo.classify("打开空调")                       // → Result.success(4) 即 "直接车控"
 *   repo.unLoad()                                  // 释放模型
 */
class OnnxLlmRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : IBertLlm {

    companion object {
        const val TAG = "OnnxLlmRepository"
        private val OnnxSingleThreadDispatcher = Dispatchers.IO.limitedParallelism(1)
    }

    /** 是否启用 NNAPI（DSP/NPU 加速）。需在 init() 前设置，init() 后修改需重新调用 init()。 */
    var useNpu: Boolean = false

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    override fun init(modelPath: String): Flow<Result<Boolean>> = flow {
        Logger.d(TAG, "init: $modelPath")
        try {
            if (!File(modelPath).exists()) {
                Logger.e(TAG, "Model file not found: $modelPath")
                emit(Result.failure(BertException.ClassifyFail()))
                return@flow
            }

            // 释放旧资源（如果重复调用 init）
            releaseSession()

            // 创建 ONNX Runtime 环境
            ortEnv = OrtEnvironment.getEnvironment()
            Logger.d(TAG, "ONNX Runtime environment created")

            // 注册 onnxruntime-extensions 自定义算子（BertTokenizer 等）
            // libonnxruntime_extensions.so 由 onnxruntime-extensions-android AAR 提供
            val options = OrtSession.SessionOptions()
            val libDir = context.applicationInfo.nativeLibraryDir
            val extensionsLib = "$libDir/libortextensions.so"
            Logger.d(TAG, "Registering extensions: $extensionsLib")
            options.registerCustomOpLibrary(extensionsLib)

            // NPU 开关：仅在用户显式开启且设备支持时启用 NNAPI
            // 注意：小模型（BERT 分类）NNAPI 调度开销可能反超纯 CPU，
            // 默认关闭，由用户根据实际设备自行选择
            if (useNpu) {
                try {
                    options.addNnapi()
                    Logger.d(TAG, "NNAPI enabled (useNpu=true)")
                } catch (e: Exception) {
                    Logger.w(TAG, "NNAPI not supported on this device, falling back to CPU")
                }
            }

            // 加载模型文件（量化 + 内置 tokenizer + argmax）
            ortSession = ortEnv?.createSession(modelPath, options)
            Logger.d(TAG, "Model loaded: $modelPath")

            emit(Result.success(true))
        } catch (e: Exception) {
            Logger.e(TAG, "init failed", e)
            releaseSession()
            emit(Result.failure(e))
        }
    }.flowOn(OnnxSingleThreadDispatcher)

    override fun classify(text: String): Flow<Result<Int>> = flow {
        Logger.d(TAG, "classify: $text")
        try {
            val env = ortEnv ?: throw IllegalStateException("ONNX environment not initialized")
            val session = ortSession ?: throw IllegalStateException("ONNX session not initialized")

            // 输入: 2D 字符串张量 [batch=1, num_sentences=1]
            val inputTensor = OnnxTensor.createTensor(env, arrayOf(arrayOf(text)))
            inputTensor.use { input ->
                // 推理 — 模型输入名固定为 "input_text"
                val outputs = session.run(mapOf("input_text" to input))
                outputs.use { result ->
                    // 输出: int64 张量，取第一个元素即分类标签索引
                    val predId = (result[0].value as LongArray)[0].toInt()
                    Logger.d(TAG, "classify result: index=$predId")
                    emit(Result.success(predId))
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "classify failed: $text", e)
            emit(Result.failure(e))
        }
    }.flowOn(OnnxSingleThreadDispatcher)

    override fun isReady(): Boolean {
        val ready = ortSession != null && ortEnv != null
        Logger.d(TAG, "isReady: $ready")
        return ready
    }

    override fun unLoad() {
        Logger.d(TAG, "unLoad")
        releaseSession()
    }

    override fun shutDown() {
        Logger.d(TAG, "shutDown")
        releaseSession()
        ortEnv?.close()
        ortEnv = null
    }

    private fun releaseSession() {
        ortSession?.close()
        ortSession = null
    }
}
