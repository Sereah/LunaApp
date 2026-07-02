package com.lunacattus.llm.model

data class ClassificationUiState(
    // BERT (llama.cpp JNI)
    val bertState: ModelState = ModelState.Idle,
    val bertModelPath: String = "",
    val bertResult: Int = -1,
    val bertLabel: String = "",
    val bertTimeMs: Long? = null,

    // ONNX Runtime
    val onnxState: ModelState = ModelState.Idle,
    val onnxModelPath: String = "",
    val onnxResult: Int = -1,
    val onnxLabel: String = "",
    val onnxTimeMs: Long? = null,
    val onnxUseNpu: Boolean = false,
)
