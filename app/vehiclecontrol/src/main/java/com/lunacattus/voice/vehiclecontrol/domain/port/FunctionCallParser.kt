package com.lunacattus.voice.vehiclecontrol.domain.port

import com.lunacattus.voice.vehiclecontrol.domain.model.VehicleControl

/**
 * 入站端口：将 function-call JSON 解析为结构化的车控命令列表。
 *
 * 实现可以是基于规则的 Regex，也可以是 LLM/意图模型，通过此接口解耦。
 */
interface FunctionCallParser {

    /**
     * 解析 function-call JSON 字符串。
     *
     * @param json 原始 function-call JSON，格式见 [com.lunacattus.voice.vehiclecontrol.domain.model.FunctionCallInput]
     * @return 解析后的 VehicleControl 列表，解析失败返回空列表
     */
    fun parse(json: String): List<VehicleControl>
}
