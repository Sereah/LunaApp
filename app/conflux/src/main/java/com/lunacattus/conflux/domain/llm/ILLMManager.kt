package com.lunacattus.conflux.domain.llm

interface ILLMManager {
    suspend fun initModel()
}