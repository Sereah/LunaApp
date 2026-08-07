package com.lunacattus.voice.vehiclecontrol.adapter.repository

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTask
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskRepository

/**
 * 基于 ConcurrentHashMap 的内存任务仓库。
 *
 * 适用于单进程场景，进程重启后数据丢失。
 */
class InMemoryControlTaskRepository : ControlTaskRepository {

    private val store = java.util.concurrent.ConcurrentHashMap<String, ControlTask>()

    override suspend fun save(task: ControlTask) {
        store[task.taskId] = task
    }

    override suspend fun get(taskId: String): ControlTask? = store[taskId]

    override suspend fun getAll(): List<ControlTask> =
        store.values.sortedByDescending { it.createdAt }

    override suspend fun delete(taskId: String) {
        store.remove(taskId)
    }
}
