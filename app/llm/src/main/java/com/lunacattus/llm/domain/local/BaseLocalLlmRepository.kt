package com.lunacattus.llm.domain.local

import com.lunacattus.llm.model.LlmException
import com.lunacattus.logger.Logger
import java.io.File

abstract class BaseLocalLlmRepository {
    protected abstract val tag: String

    fun loadModel(
        modelPath: String,
        nativeLoadModel: (String) -> Int,
        nativePrepare: () -> Int
    ): Result<Boolean> {
        Logger.d(tag, "loadModel: $modelPath")
        if (!File(modelPath).exists()) {
            Logger.e(tag, "Model file not found: $modelPath")
            return Result.failure(LlmException.ModelPathNull())
        }
        if (nativeLoadModel(modelPath) != 0) return Result.failure(LlmException.ModelLoadFail())
        if (nativePrepare() != 0) return Result.failure(LlmException.ModelPrepareFail())
        return Result.success(true)
    }
}
