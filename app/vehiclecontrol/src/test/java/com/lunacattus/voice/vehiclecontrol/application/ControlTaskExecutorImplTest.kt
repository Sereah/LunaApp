package com.lunacattus.voice.vehiclecontrol.application

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlResult
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlState
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTarget
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTask
import com.lunacattus.voice.vehiclecontrol.domain.model.TaskState
import com.lunacattus.voice.vehiclecontrol.domain.model.VehicleControl
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlStateObserver
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskExecutor
import com.lunacattus.voice.vehiclecontrol.domain.port.ControlTaskRepository
import com.lunacattus.voice.vehiclecontrol.domain.port.VehicleControlAdapter
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ControlTaskExecutorImplTest {

    private lateinit var adapter: VehicleControlAdapter
    private lateinit var repository: ControlTaskRepository
    private lateinit var observer: ControlStateObserver
    private lateinit var executor: ControlTaskExecutor

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        adapter = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        observer = mockk(relaxed = true)
        executor = ControlTaskExecutorImpl(
            adapter = adapter,
            repository = repository,
            observers = listOf(observer),
            dispatcher = testDispatcher,
        )
    }

    // ── 辅助方法 ───────────────────────────────────────────────

    private fun mockAdapterExecute(control: VehicleControl, vararg states: ControlState) {
        io.mockk.every { adapter.execute(control) } returns flow {
            states.forEach { emit(ControlResult(control.id, it)) }
        }
    }

    private fun createTask(vararg actions: Pair<ControlTarget, String>): ControlTask {
        val controls = actions.map { (target, action) ->
            VehicleControl(target = target, action = action)
        }
        return ControlTask(rawInput = "{}", controls = controls)
    }

    // ── Happy Path ─────────────────────────────────────────────

    @Test
    fun `Given 单个 control 任务 When execute Then 最终状态 SUCCESS`() = runTest {
        val task = createTask(ControlTarget.AC to "set_temperature")
        mockAdapterExecute(task.controls[0], ControlState.EXECUTING, ControlState.SUCCESS)

        val snapshots = executor.execute(task).toList()

        val finalTask = snapshots.last()
        assertEquals(TaskState.SUCCESS, finalTask.taskState)
        assertEquals(1, finalTask.results.size)
    }

    @Test
    fun `Given 多个 control 任务 When execute Then 并发执行且最终状态 SUCCESS`() = runTest {
        val task = createTask(
            ControlTarget.AC to "set_temperature",
            ControlTarget.WINDOW to "close",
        )
        mockAdapterExecute(task.controls[0], ControlState.EXECUTING, ControlState.SUCCESS)
        mockAdapterExecute(task.controls[1], ControlState.EXECUTING, ControlState.SUCCESS)

        val snapshots = executor.execute(task).toList()

        val finalTask = snapshots.last()
        assertEquals(TaskState.SUCCESS, finalTask.taskState)
        assertEquals(2, finalTask.results.size)
    }

    @Test
    fun `Given 部分 control 失败 When execute Then 最终状态 PARTIAL_SUCCESS`() = runTest {
        val task = createTask(
            ControlTarget.AC to "set_temperature",
            ControlTarget.WINDOW to "close",
        )
        mockAdapterExecute(task.controls[0], ControlState.EXECUTING, ControlState.SUCCESS)
        mockAdapterExecute(task.controls[1], ControlState.EXECUTING, ControlState.FAILED)

        val snapshots = executor.execute(task).toList()

        val finalTask = snapshots.last()
        assertEquals(TaskState.PARTIAL_SUCCESS, finalTask.taskState)
    }

    @Test
    fun `Given 全部 control 失败 When execute Then 最终状态 FAILED`() = runTest {
        val task = createTask(ControlTarget.AC to "set_temperature")
        mockAdapterExecute(task.controls[0], ControlState.EXECUTING, ControlState.FAILED)

        val snapshots = executor.execute(task).toList()

        val finalTask = snapshots.last()
        assertEquals(TaskState.FAILED, finalTask.taskState)
    }

    // ── 观察者通知 ─────────────────────────────────────────────

    @Test
    fun `Given 注册了 observer When 任务完成 Then observer 收到通知`() = runTest {
        val task = createTask(ControlTarget.LIGHT to "turn_on")
        mockAdapterExecute(task.controls[0], ControlState.EXECUTING, ControlState.SUCCESS)

        executor.execute(task).toList()

        verify(atLeast = 1) { observer.onTaskStateChanged(any()) }
        verify(atLeast = 1) { observer.onControlResult(any()) }
    }

    // ── 持久化 ─────────────────────────────────────────────────

    @Test
    fun `Given 任务执行 When 状态变更 Then 自动持久化`() = runTest {
        val task = createTask(ControlTarget.TRUNK to "open")
        mockAdapterExecute(task.controls[0], ControlState.EXECUTING, ControlState.SUCCESS)

        executor.execute(task).toList()

        coVerify(atLeast = 1) { repository.save(any()) }
    }

    // ── 空任务 ─────────────────────────────────────────────────

    @Test
    fun `Given 空 controls 任务 When execute Then 直接 SUCCESS`() = runTest {
        val task = ControlTask(rawInput = "{}", controls = emptyList())

        val snapshots = executor.execute(task).toList()

        assertEquals(TaskState.SUCCESS, snapshots.last().taskState)
    }
}
