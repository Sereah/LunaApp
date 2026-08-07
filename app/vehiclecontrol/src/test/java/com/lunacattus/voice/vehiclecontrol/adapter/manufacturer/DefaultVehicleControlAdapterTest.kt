package com.lunacattus.voice.vehiclecontrol.adapter.manufacturer

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlState
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTarget
import com.lunacattus.voice.vehiclecontrol.domain.model.VehicleControl
import com.lunacattus.voice.vehiclecontrol.domain.port.VehicleControlAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DefaultVehicleControlAdapterTest {

    private lateinit var adapter: VehicleControlAdapter

    @Before
    fun setUp() {
        adapter = DefaultVehicleControlAdapter(executionDelayMs = 0L)
    }

    // ── Happy Path ─────────────────────────────────────────────

    @Test
    fun `Given adapter 可用 When execute ac control Then 依次发射 EXECUTING → SUCCESS`() = runTest {
        val control = VehicleControl(
            target = ControlTarget.AC,
            action = "set_temperature",
            parameters = mapOf("temperature" to "24"),
        )

        val results = adapter.execute(control).toList()

        assertEquals(2, results.size)
        assertEquals(ControlState.EXECUTING, results[0].state)
        assertEquals(ControlState.SUCCESS, results[1].state)
    }

    @Test
    fun `Given 正常执行 When 执行完成 Then 结果包含 controlId 和时间戳`() = runTest {
        val control = VehicleControl(
            id = "ctrl-001",
            target = ControlTarget.WINDOW,
            action = "close",
        )

        val results = adapter.execute(control).toList()

        assertEquals(2, results.size)
        assertEquals("ctrl-001", results[0].controlId)
        assertEquals("ctrl-001", results[1].controlId)
        assertTrue(results[1].timestamp > 0)
    }

    @Test
    fun `Given 成功结果 When 执行完成 Then message 不为空`() = runTest {
        val control = VehicleControl(target = ControlTarget.TRUNK, action = "open")

        val results = adapter.execute(control).toList()

        val successResult = results.find { it.state == ControlState.SUCCESS }
        assertNotNull(successResult)
        assertTrue(successResult!!.message?.contains("执行成功") == true)
    }

    // ── 取消 ───────────────────────────────────────────────────

    @Test
    fun `Given 正在执行 When cancel 被调用 Then EXECUTING 后发射 CANCELLED`() = runBlocking {
        val slowAdapter = DefaultVehicleControlAdapter(executionDelayMs = 2000L)
        val control = VehicleControl(target = ControlTarget.HORN, action = "honk")

        val results = mutableListOf<com.lunacattus.voice.vehiclecontrol.domain.model.ControlResult>()

        // 用真实时间的协程来收集 flow
        val job = launch(Dispatchers.Default) {
            slowAdapter.execute(control).collect { results.add(it) }
        }

        // 等待 EXECUTING 被发出
        kotlinx.coroutines.delay(50)

        // 调用 cancel
        slowAdapter.cancel(control.id)

        // 等待 CANCELLED 被发出
        kotlinx.coroutines.delay(200)

        job.cancel()

        assertTrue("应包含 EXECUTING: $results", results.any { it.state == ControlState.EXECUTING })
        assertTrue("应包含 CANCELLED: $results", results.any { it.state == ControlState.CANCELLED })
    }

    // ── isAvailable / manufacturerId ───────────────────────────

    @Test
    fun `Given 默认适配器 When isAvailable Then 返回 true`() {
        assertTrue(adapter.isAvailable())
    }

    @Test
    fun `Given 默认适配器 When manufacturerId Then 返回 default`() {
        assertEquals("default", adapter.manufacturerId)
    }
}
