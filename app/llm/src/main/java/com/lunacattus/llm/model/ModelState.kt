package com.lunacattus.llm.model

sealed class ModelState {
    data object Idle : ModelState()
    data object Loading : ModelState()
    data object Loaded : ModelState()
    data class Error(val msg: String) : ModelState()
}