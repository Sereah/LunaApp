package com.lunacattus.voice.vehiclecontrol.application

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlResult
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlState
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTask
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlStateObserver
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskExecutor
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskRepository
import com.lunacattus.voice.vehiclecontrol.domain.port.VehicleControlAdapter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * 车控任务执行器实现。
 *
 * 一个 [ControlTask] 内的多个 [VehicleControl] 并发执行。
 * 每次状态变更通过 [Flow] 发射最新任务快照，到达终态时 Flow 自动关闭。
 */
class ControlTaskExecutorImpl(
    private val adapter: VehicleControlAdapter,
    private val repository: ControlTaskRepository,
    private val observers: List<ControlStateObserver> = emptyList(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ControlTaskExecutor {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val cancellationTokens = ConcurrentHashMap<String, Boolean>()
    private val stateMutex = Mutex()

    override fun execute(task: ControlTask): Flow<ControlTask> {
        val channel = Channel<ControlTask>(Channel.UNLIMITED)

        scope.launch {
            var current = task
            cancellationTokens[task.taskId] = false

            try {
                channel.send(current)

                if (current.controls.isEmpty()) {
                    val completed = current.copy(
                        results = current.controls.associate { it.id to
                            ControlResult(it.id, ControlState.SUCCESS) }
                    )
                    channel.send(completed)
                    notifyAndPersist(completed)
                    return@launch
                }

                // 并发执行所有 control
                val childJobs = current.controls.map { control ->
                    launch {
                        if (cancellationTokens[task.taskId] == true) return@launch
                        adapter.execute(control).collect { result ->
                            stateMutex.withLock {
                                current = current.withResult(result)
                            }
                            channel.send(current)
                            notifyAndPersist(current)

                            if (result.state.isTerminal()) return@collect
                        }
                    }
                }

                childJobs.joinAll()
            } finally {
                cancellationTokens.remove(task.taskId)
                channel.close()
            }
        }

        return channel.receiveAsFlow()
    }

    override fun cancel(taskId: String) {
        cancellationTokens[taskId] = true
    }

    private suspend fun notifyAndPersist(task: ControlTask) {
        observers.forEach { observer ->
            try { observer.onTaskStateChanged(task) } catch (_: Exception) { }
        }
        task.results.values.forEach { result ->
            observers.forEach { observer ->
                try { observer.onControlResult(result) } catch (_: Exception) { }
            }
        }
        try { repository.save(task) } catch (_: Exception) { }
    }
}

private fun ControlState.isTerminal(): Boolean = this == ControlState.SUCCESS ||
        this == ControlState.FAILED || this == ControlState.TIMEOUT ||
        this == ControlState.CANCELLED
