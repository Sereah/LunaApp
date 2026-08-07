package com.lunacattus.voice.vehiclecontrol.domain.model

/**
 * 单个车控操作的执行结果。
 *
 * @param controlId 对应的 VehicleControl.id
 * @param state 最终状态
 * @param message 可选错误或状态描述
 * @param timestamp 结果产生的时间戳
 */
data class ControlResult(
    val controlId: String,
    val state: ControlState,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)
