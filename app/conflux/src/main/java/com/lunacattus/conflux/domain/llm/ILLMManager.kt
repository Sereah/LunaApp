package com.lunacattus.conflux.domain.llm

import kotlinx.coroutines.flow.Flow

interface ILLMManager {
    suspend fun initModel(): Boolean

    suspend fun generate(prompt: String): Flow<String>

    fun release()
}