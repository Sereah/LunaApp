package com.lunacattus.conflux.domain.llm.polish

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PolishRepository @Inject constructor(
    private val httpService: PolishHttpService,
) : IPolish {

    override fun updateConfig(config: PolishConfig) {
        httpService.updateConfig(config)
    }

    override suspend fun polish(request: PolishRequest): Result<PolishResponse> {
        return httpService.polish(request)
    }
}
