package com.lunacattus.voice.vehiclecontrol.domain.model

/**
 * 单个 function-call 的结构化表示。
 *
 * 示例 JSON：
 * ```json
 * {
 *   "function": "ac_control",
 *   "arguments": { "action": "set_temperature", "temperature": 26 }
 * }
 * ```
 *
 * @param function 函数名，映射到具体车控操作
 * @param arguments 函数参数
 */
data class FunctionCall(
    val function: String,
    val arguments: Map<String, String> = emptyMap(),
)

/**
 * 外部传入的 function-call 输入顶层结构。
 *
 * 示例 JSON：
 * ```json
 * {
 *   "calls": [
 *     { "function": "ac_control", "arguments": { ... } },
 *     { "function": "window_control", "arguments": { ... } }
 *   ]
 * }
 * ```
 */
data class FunctionCallInput(
    val calls: List<FunctionCall>,
)
