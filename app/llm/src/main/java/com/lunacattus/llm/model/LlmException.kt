package com.lunacattus.llm.model

sealed class LlmException : Exception() {
    class ModelPathNull : LlmException()
    class ModelLoadFail : LlmException()
    class ModelPrepareFail : LlmException()
}

sealed class GenerateException : LlmException() {
    class ProcessUserPromptFail : LlmException()
}

sealed class BertException : LlmException() {
    class ClassifyFail : BertException()
}

