package com.lunacattus.llm.domain.base

import kotlinx.coroutines.flow.Flow

interface IBertLlm : ILlm {
    fun classify(text: String): Flow<Result<Int>>
}