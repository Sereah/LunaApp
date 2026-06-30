package com.lunacattus.llm.domain.base

import kotlinx.coroutines.flow.Flow

interface ILlm {
    fun init(modelPath: String): Flow<Result<Boolean>>

    fun isReady(): Boolean

    fun unLoad()

    fun shutDown()
}