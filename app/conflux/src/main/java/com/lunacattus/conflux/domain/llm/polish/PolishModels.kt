package com.lunacattus.conflux.domain.llm.polish

import com.google.gson.annotations.SerializedName

data class PolishRequest(
    @SerializedName("requestID") val requestId: String,
    val txt: String,
    val temperature: Double,
)

data class PolishResponse(
    @SerializedName("requestID") val requestId: String,
    val txt: String,
)
