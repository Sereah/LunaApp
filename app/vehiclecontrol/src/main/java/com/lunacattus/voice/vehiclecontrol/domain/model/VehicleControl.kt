package com.lunacattus.voice.vehiclecontrol.domain.model

import java.util.UUID

/**
 * 单个车控操作的值对象。
 *
 * @param id 唯一标识
 * @param target 控制目标设备
 * @param action 操作名称（如 "set_temperature", "open", "close"）
 * @param parameters 操作参数键值对（如 {"temperature": "26", "unit": "celsius"}）
 */
data class VehicleControl(
    val id: String = UUID.randomUUID().toString(),
    val target: ControlTarget,
    val action: String,
    val parameters: Map<String, String> = emptyMap(),
)
