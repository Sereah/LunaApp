package com.lunacattus.conflux.domain.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LunaLlmManager @Inject constructor() : ILLMManager {

    companion object {
        private const val TAG = "LunaLlmManager"
        private const val MODEL_PATH = "/data/local/tmp/llm/luna_car_gemma4_v1.gguf"
    }

    override suspend fun initModel(): Boolean {
        return true
    }

    override suspend fun generate(prompt: String): Flow<String> {
        return withContext(Dispatchers.IO) {
            flowOf("")
        }
    }

    override fun release() {

    }
}
