package com.lunacattus.llm.domain.local

import android.content.Context
import com.lunacattus.llm.model.LlmException
import com.lunacattus.logger.Logger
import java.io.File
import java.io.IOException

abstract class BaseLocalLlmRepository {
    protected abstract val tag: String
    protected abstract val modelFileName: String

    fun loadModel(
        context: Context,
        nativeLoadModel: (String) -> Int,
        nativePrepare: () -> Int
    ): Result<Boolean> {
        val externalModelFile =
            File(File(context.getExternalFilesDir(null), "gguf"), modelFileName)
        val modelPath: String? = if (externalModelFile.exists()) {
            Logger.d(tag, "loadModel from external: ${externalModelFile.absolutePath}")
            externalModelFile.absolutePath
        } else {
            val internalModelFile = File(File(context.filesDir, "gguf"), modelFileName)
            if (!internalModelFile.exists()) {
                Logger.d(tag, "Model not found, copying $modelFileName from assets...")
                copyModelFromAssets(context)
            }
            if (internalModelFile.exists()) internalModelFile.absolutePath else null
        }

        Logger.d(tag, "loadModel: $modelPath")
        if (modelPath == null) {
            Logger.e(tag, "Model file not found: $modelFileName")
            return Result.failure(LlmException.ModelPathNull())
        }
        if (nativeLoadModel(modelPath) != 0) return Result.failure(LlmException.ModelLoadFail())
        if (nativePrepare() != 0) return Result.failure(LlmException.ModelPrepareFail())
        return Result.success(true)
    }

    private fun copyModelFromAssets(context: Context) {
        val dest = File(File(context.filesDir, "gguf"), modelFileName)
        dest.parentFile?.mkdirs()
        try {
            context.assets.open("gguf/$modelFileName").use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Logger.d(tag, "Copied $modelFileName from assets")
        } catch (e: IOException) {
            Logger.e(tag, "Failed to copy $modelFileName from assets", e)
            dest.delete()
        }
    }
}
