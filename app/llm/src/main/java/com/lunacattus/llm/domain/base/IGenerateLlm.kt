package com.lunacattus.llm.domain.base

import kotlinx.coroutines.flow.Flow

interface IGenerateLlm : ILlm {
    fun sendSystemPrompt(prompt: String, enableThinking: Boolean = false): Flow<Result<Boolean>>

    fun sendUserPrompt(
        prompt: String,
        predictLength: Int,
        enableThinking: Boolean = false
    ): Flow<Result<String>>

}