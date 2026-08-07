package com.lunacattus.voice.vehiclecontrol.domain.port

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTask

/**
 * 出站端口（SPI）：车控任务持久化仓库。
 *
 * 用于保存和查询历史任务记录。
 */
interface ControlTaskRepository {

    /** 保存或更新一个任务 */
    suspend fun save(task: ControlTask)

    /** 按 ID 查询任务 */
    suspend fun get(taskId: String): ControlTask?

    /** 获取全部任务，按创建时间倒序 */
    suspend fun getAll(): List<ControlTask>

    /** 删除指定任务 */
    suspend fun delete(taskId: String)
}
