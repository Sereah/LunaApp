package com.lunacattus.llm.domain.local

import android.content.Context
import com.lunacattus.llm.domain.base.IGenerateLlm
import com.lunacattus.llm.model.GenerateException
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.annotation.optimization.FastNative
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GenerateLLmRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : IGenerateLlm, BaseLocalLlmRepository() {

    companion object {
        const val TAG = "GenerateLLmRepository"
        private val LlmSingleThreadDispatcher = Dispatchers.IO.limitedParallelism(1)
    }

    override val tag = TAG

    override fun init(modelPath: String): Flow<Result<Boolean>> = flow {
        Logger.d(TAG, "init: $modelPath")
        try {
            System.loadLibrary("luna_llm")
            nativeInit(context.applicationInfo.nativeLibraryDir)
            emit(loadModel(modelPath, ::nativeLoadModel, ::nativePrepare))
        } catch (e: Exception) {
            Logger.e(TAG, "init failed", e)
            emit(Result.failure(e))
        }
    }.flowOn(LlmSingleThreadDispatcher)

    override fun isReady(): Boolean {
        val isReady = nativeIsReady()
        Logger.d(TAG, "isModelReady: $isReady")
        return isReady
    }

    override fun sendSystemPrompt(
        prompt: String,
        enableThinking: Boolean
    ): Flow<Result<Boolean>> = flow {
        try {
            val resultCode = nativeProcessSystemPrompt(prompt, enableThinking)
            emit(Result.success(resultCode == 0))
        } catch (e: Exception) {
            Logger.e(TAG, "sendSystemPrompt failed", e)
            emit(Result.failure(e))
        }
    }.flowOn(LlmSingleThreadDispatcher)

    override fun sendUserPrompt(
        prompt: String,
        predictLength: Int,
        enableThinking: Boolean
    ): Flow<Result<String>> = flow {
        try {
            val resultCode = nativeProcessUserPrompt(prompt, predictLength, enableThinking)
            if (resultCode == 0) {
                while (true) {
                    nativeGenerateNextToken()?.let {
                        if (it.isNotEmpty()) emit(Result.success(it))
                    } ?: break
                }
            } else {
                emit(Result.failure(GenerateException.ProcessUserPromptFail()))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "sendUserPrompt failed", e)
            emit(Result.failure(e))
        }
    }.flowOn(LlmSingleThreadDispatcher)

    override fun unLoad() {
        Logger.d(TAG, "unLoad")
        nativeUnload()
    }

    override fun shutDown() {
        Logger.d(TAG, "shutDown")
        nativeShutdown()
    }

    @FastNative
    private external fun nativeInit(nativeLibPath: String)

    @FastNative
    private external fun nativeLoadModel(modelPath: String): Int

    @FastNative
    private external fun nativePrepare(): Int

    @FastNative
    private external fun nativeIsReady(): Boolean

    @FastNative
    private external fun nativeProcessSystemPrompt(
        systemPrompt: String,
        enableThinking: Boolean
    ): Int

    @FastNative
    private external fun nativeProcessUserPrompt(
        userPrompt: String,
        predictLength: Int,
        enableThinking: Boolean
    ): Int

    @FastNative
    private external fun nativeGenerateNextToken(): String?

    @FastNative
    private external fun nativeUnload()

    @FastNative
    private external fun nativeShutdown()
}
