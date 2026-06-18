package com.lunacattus.llm.domain.local

import android.content.Context
import com.lunacattus.common.utils.AssetUtils
import com.lunacattus.llm.domain.ILlm
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
            emit(Result.success(result))
        } catch (e: Exception) {
            Logger.e(TAG, "init failed", e)
            emit(Result.failure(e))
        }
    }.flowOn(LlmSingleThreadDispatcher)

    private fun initModel() {
        Logger.d(TAG, "initModel")
        System.loadLibrary("luna_llm")
        initModel(context.applicationInfo.nativeLibraryDir)
    }

    private fun loadModel(): Boolean {
        val modelDir = AssetUtils.copyToFiles(context, "gguf")
        val modelPath = if (modelDir != null) {
            val dir = File(modelDir)
            dir.listFiles()?.firstOrNull { it.name.endsWith(".gguf") }?.absolutePath
        } else {
            null
        }
        Logger.d(TAG, "loadModel: $modelPath")
        val result = if (modelPath != null) {
            loadModel(modelPath)
        } else {
            1
        }
        Logger.d(TAG, "loadModel result: $result")
        return result == 0
    }

    @FastNative
    private external fun initModel(nativeLibPath: String)

    @FastNative
    private external fun loadModel(modelPath: String): Int

    @FastNative
    private external fun prepare(): Int
}