package com.lunacattus.conflux.ui.sections.llm.polish

import com.lunacattus.conflux.domain.llm.polish.PolishConfig

data class PolishState(
    val host: String = PolishConfig.DEFAULT_HOST,
    val port: String = PolishConfig.DEFAULT_PORT.toString(),
    val inputText: String = "",
    val temperature: Double = 0.85,
    val isRequesting: Boolean = false,
    val results: List<PolishResultItem> = emptyList(),
)

data class PolishResultItem(
    val id: String,
    val requestId: String,
    val originalText: String,
    val polishedText: String,
    val temperature: Double,
    val timestamp: Long,
)

sealed interface PolishIntent {
    data class UpdateHost(val host: String) : PolishIntent
    data class UpdatePort(val port: String) : PolishIntent
    data class UpdateText(val text: String) : PolishIntent
    data class UpdateTemperature(val temperature: Double) : PolishIntent
    data class SelectPreset(val text: String) : PolishIntent
    data object SendPolish : PolishIntent
    data class DeleteResult(val id: String) : PolishIntent
    data object ClearAll : PolishIntent
}

sealed interface PolishEffect {
    data object ScrollToBottom : PolishEffect
}

val POLISH_PRESETS = listOf(
    "今天天气真好，适合出去玩",
    "我非常喜欢这个产品，它的功能很强大",
    "请帮我写一份关于人工智能发展趋势的报告摘要",
    "这段时间工作压力很大，但我觉得一切都在慢慢变好",
    "春眠不觉晓，处处闻啼鸟，夜来风雨声，花落知多少",
    "这道菜的做法其实很简单，先把食材准备好，然后按顺序下锅就可以了",
)
