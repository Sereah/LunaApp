package com.lunacattus.llm.domain

import kotlinx.coroutines.flow.Flow

interface ILlm {
    fun init(): Flow<Result<Boolean>>

    fun isModelReady(): Boolean
}