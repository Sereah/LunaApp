package com.lunacattus.llm.domain.local

import android.content.Context
import com.lunacattus.llm.domain.base.IBertLlm
import com.lunacattus.llm.model.BertException
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.annotation.optimization.FastNative
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class BertLLmRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : IBertLlm, BaseLocalLlmRepository() {

    companion object {
        const val TAG = "BertLLmRepository"
        private val EncoderSingleThreadDispatcher = Dispatchers.IO.limitedParallelism(1)
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
    }.flowOn(EncoderSingleThreadDispatcher)

    override fun isReady(): Boolean {
        val ready = nativeIsReady()
        Logger.d(TAG, "isReady: $ready")
        return ready
    }

    override fun unLoad() {
        Logger.d(TAG, "unLoad")
        nativeUnload()
    }

    override fun shutDown() {
        Logger.d(TAG, "shutDown")
        nativeShutdown()
    }

    override fun classify(text: String): Flow<Result<Int>> = flow {
        Logger.d(TAG, "classify: $text")
        try {
            val idx = nativeClassifyText(text)
            if (idx < 0) {
                emit(Result.failure(BertException.ClassifyFail()))
            } else {
                emit(Result.success(idx))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "classify failed", e)
            emit(Result.failure(e))
        }
    }.flowOn(EncoderSingleThreadDispatcher)

    @FastNative
    private external fun nativeInit(nativeLibPath: String)

    @FastNative
    private external fun nativeLoadModel(modelPath: String): Int

    @FastNative
    private external fun nativePrepare(): Int

    @FastNative
    private external fun nativeIsReady(): Boolean

    @FastNative
    private external fun nativeClassifyText(text: String): Int

    @FastNative
    private external fun nativeUnload()

    @FastNative
    private external fun nativeShutdown()
}
