package com.lunacattus.llm.model

data class UiState(
    val generateState: ModelState = ModelState.Idle,
    val bertState: ModelState = ModelState.Idle,
    val generateModelPath: String = "",
    val bertModelPath: String = "",
    val systemPromptReady: Boolean = false,
    val assistantResponse: String = "",
    val classificationResult: Int = -1,
    val classificationLabel: String = "",
    val classificationTimeMs: Long? = null,
    val responseTimeMs: Long? = null
)