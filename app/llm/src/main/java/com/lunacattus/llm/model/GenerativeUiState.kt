package com.lunacattus.llm.model

data class GenerativeUiState(
    val generateState: ModelState = ModelState.Idle,
    val generateModelPath: String = "",
    val systemPromptReady: Boolean = false,
    val assistantResponse: String = "",
    val responseTimeMs: Long? = null,
)
