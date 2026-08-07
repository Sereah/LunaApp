package com.lunacattus.voice.vehiclecontrol.domain.model

/**
 * 单个车控操作的执行状态。
 */
enum class ControlState {
    PENDING,
    EXECUTING,
    SUCCESS,
    FAILED,
    TIMEOUT,
    CANCELLED,
}
