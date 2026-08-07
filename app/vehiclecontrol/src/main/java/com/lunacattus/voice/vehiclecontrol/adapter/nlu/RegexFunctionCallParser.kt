package com.lunacattus.voice.vehiclecontrol.adapter.nlu

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTarget
import com.lunacattus.voice.vehiclecontrol.domain.model.FunctionCall
import com.lunacattus.voice.vehiclecontrol.domain.model.FunctionCallInput
import com.lunacattus.voice.vehiclecontrol.domain.model.VehicleControl
import com.lunacattus.voice.vehiclecontrol.domain.port.FunctionCallParser

/**
 * 基于 function-name → ControlTarget 映射的 function-call 解析器。
 *
 * 根据 function 名称查表映射到 [ControlTarget]，arguments 直接作为 parameters 传递。
 * 未知的 function 名称会被静默跳过。
 */
class RegexFunctionCallParser : FunctionCallParser {

    private val gson = Gson()

    /** function 名称 → ControlTarget 的映射表 */
    private val functionMapping: Map<String, ControlTarget> = mapOf(
        "ac_control" to ControlTarget.AC,
        "window_control" to ControlTarget.WINDOW,
        "sunroof_control" to ControlTarget.SUNROOF,
        "seat_control" to ControlTarget.SEAT,
        "light_control" to ControlTarget.LIGHT,
        "door_lock_control" to ControlTarget.DOOR_LOCK,
        "trunk_control" to ControlTarget.TRUNK,
        "horn_control" to ControlTarget.HORN,
        "wiper_control" to ControlTarget.WIPER,
        "mirror_control" to ControlTarget.MIRROR,
    )

    override fun parse(json: String): List<VehicleControl> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return emptyList()

        return try {
            val inputType = object : TypeToken<FunctionCallInput>() {}.type
            val input: FunctionCallInput = gson.fromJson(trimmed, inputType)
            input.calls.mapNotNull { call -> mapToVehicleControl(call) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mapToVehicleControl(call: FunctionCall): VehicleControl? {
        val target = functionMapping[call.function] ?: return null
        return VehicleControl(
            target = target,
            action = call.arguments["action"] ?: call.function,
            parameters = call.arguments,
        )
    }
}
