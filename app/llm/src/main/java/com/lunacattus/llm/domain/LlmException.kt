package com.lunacattus.llm.domain

sealed class LlmException : Exception() {
    class ModelPathNull : LlmException()
    class ModelLoadFail: LlmException()
    class ModelPrepareFail: LlmException()
}