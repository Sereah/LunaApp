package com.lunacattus.llm.domain.local

import android.content.Context
import com.lunacattus.common.utils.AssetUtils
import com.lunacattus.llm.domain.ILlm
import com.lunacattus.llm.domain.LlmException
import com.lunacattus.logger.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.annotation.optimization.FastNative
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class LlmCppRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ILlm {

    companion object {
        const val TAG = "LlmCppRepository"
        private val LlmSingleThreadDispatcher = Dispatchers.IO.limitedParallelism(1)
    }

    override fun init(): Flow<Result<Boolean>> = flow {
        Logger.d(TAG, "init")
        try {
            initModel()
            val result = loadModel()
            emit(result)
        } catch (e: Exception) {
            Logger.e(TAG, "init failed", e)
            emit(Result.failure(e))
        }
    }.flowOn(LlmSingleThreadDispatcher)

    override fun isModelReady(): Boolean  {
        val isReady = isReady()
        Logger.d(TAG, "isModelReady: $isReady")
        return isReady
    }

    private fun initModel() {
        Logger.d(TAG, "initModel")
        System.loadLibrary("luna_llm")
        initModel(context.applicationInfo.nativeLibraryDir)
    }

    private fun loadModel(): Result<Boolean> {
        val modelDir = AssetUtils.copyToFiles(context, "gguf")
        val modelPath = if (modelDir != null) {
            val dir = File(modelDir)
            dir.listFiles()?.firstOrNull { it.name.endsWith(".gguf") }?.absolutePath
        } else {
            null
        }
        Logger.d(TAG, "loadModel: $modelPath")
        if (modelPath == null) {
            return Result.failure(LlmException.ModelPathNull())
        }
        if (loadModel(modelPath) != 0) {
            return Result.failure(LlmException.ModelLoadFail())
        }
        if (prepare() != 0) {
            return Result.failure(LlmException.ModelPrepareFail())
        }
        return Result.success(true)
    }

    @FastNative
    private external fun initModel(nativeLibPath: String)

    @FastNative
    private external fun loadModel(modelPath: String): Int

    @FastNative
    private external fun prepare(): Int

    @FastNative
    private external fun isReady(): Boolean
}