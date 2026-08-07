package com.lunacattus.voice.vehiclecontrol.domain.model

/**
 * 车控任务的整体执行状态。
 */
enum class TaskState {
    PENDING,
    EXECUTING,
    PARTIAL_SUCCESS,
    SUCCESS,
    FAILED,
    CANCELLED,
}
