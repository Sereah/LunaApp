package com.lunacattus.conflux.domain.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ILLMManager {

    private var llmInference: LlmInference? = null

    override suspend fun initModel() {
        withContext(Dispatchers.IO) {
            val taskOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath("/data/local/tmp/llm/model_version.task")
                .build()
            llmInference = LlmInference.createFromOptions(context, taskOptions)
        }
    }
}