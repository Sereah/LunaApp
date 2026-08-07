package com.lunacattus.voice.vehiclecontrol.domain.model

import java.util.UUID

/**
 * 车控任务聚合根。
 *
 * 一个任务包含一个或多个 [VehicleControl]，由同一条 function-call 输入解析而来。
 *
 * @param taskId 任务唯一标识
 * @param rawInput 原始 function-call JSON 字符串
 * @param controls 要执行的车控操作列表
 * @param createdAt 任务创建时间戳
 * @param results controlId → ControlResult 的执行结果映射
 */
data class ControlTask(
    val taskId: String = UUID.randomUUID().toString(),
    val rawInput: String,
    val controls: List<VehicleControl>,
    val createdAt: Long = System.currentTimeMillis(),
    val results: Map<String, ControlResult> = emptyMap(),
) {

    /**
     * 根据当前所有 control 的执行结果推导任务整体状态。
     */
    val taskState: TaskState
        get() {
            if (controls.isEmpty()) return TaskState.SUCCESS
            val states = controls.map { results[it.id]?.state }

            return when {
                states.all { it == null } -> TaskState.PENDING
                states.any { it == ControlState.EXECUTING } -> TaskState.EXECUTING
                states.any { it == ControlState.CANCELLED } && states.none { it == ControlState.EXECUTING } -> TaskState.CANCELLED
                states.all { it == ControlState.SUCCESS } -> TaskState.SUCCESS
                states.any { it == ControlState.SUCCESS } && states.any { it == ControlState.FAILED || it == ControlState.TIMEOUT } -> TaskState.PARTIAL_SUCCESS
                states.all { it == ControlState.FAILED || it == ControlState.TIMEOUT } -> TaskState.FAILED
                else -> TaskState.EXECUTING
            }
        }

    /**
     * 更新某个 control 的执行结果，返回新的 ControlTask 快照。
     */
    fun withResult(result: ControlResult): ControlTask =
        copy(results = results + (result.controlId to result))
}
