package com.lunacattus.conflux.domain.llm.polish

interface IPolish {
    fun updateConfig(config: PolishConfig)

    suspend fun polish(request: PolishRequest): Result<PolishResponse>
}
