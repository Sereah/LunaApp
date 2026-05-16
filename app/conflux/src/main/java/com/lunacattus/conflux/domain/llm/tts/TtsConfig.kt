package com.lunacattus.conflux.domain.llm.tts

data class TtsConfig(
    val host: String = DEFAULT_HOST,
    val port: Int = DEFAULT_PORT,
) {
    val httpBaseUrl: String get() = "http://$host:$port"
    val wsUrl: String get() = "ws://$host:$port"

    companion object {
        const val DEFAULT_HOST = "home.lunacattus.com"
        const val DEFAULT_PORT = 8000
    }
}
