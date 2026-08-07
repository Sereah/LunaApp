package com.lunacattus.voice.vehiclecontrol.domain.port

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTask
import kotlinx.coroutines.flow.Flow

/**
 * 入站端口：车控任务执行器。
 *
 * 负责编排任务中多个 [com.lunacattus.voice.vehiclecontrol.domain.model.VehicleControl] 的并发执行，
 * 通过 [Flow] 发射每次状态变更。
 */
interface ControlTaskExecutor {

    /**
     * 执行一个车控任务。
     *
     * @param task 待执行的任务
     * @return Flow，每次任务状态变更时发射最新的 ControlTask 快照
     */
    fun execute(task: ControlTask): Flow<ControlTask>

    /**
     * 取消指定任务。
     *
     * @param taskId 任务 ID
     */
    fun cancel(taskId: String)
}
