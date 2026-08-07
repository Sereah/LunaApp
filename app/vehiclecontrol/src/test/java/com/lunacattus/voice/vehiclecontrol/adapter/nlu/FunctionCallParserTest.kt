package com.lunacattus.voice.vehiclecontrol.adapter.nlu

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTarget
import com.lunacattus.voice.vehiclecontrol.domain.port.FunctionCallParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * FunctionCallParser 的 BDD 测试。
 *
 * 测试解析 function-call JSON → List<VehicleControl> 的各种场景。
 */
class FunctionCallParserTest {

    private lateinit var parser: FunctionCallParser

    @Before
    fun setUp() {
        parser = RegexFunctionCallParser()
    }

    // ── Happy Path ─────────────────────────────────────────────

    @Test
    fun `Given 单个 ac_control function-call When parse Then 返回1个 VehicleControl`() {
        val json = """
            {
                "calls": [
                    {
                        "function": "ac_control",
                        "arguments": {
                            "action": "set_temperature",
                            "temperature": "26"
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = parser.parse(json)

        assertEquals(1, result.size)
        assertEquals(ControlTarget.AC, result[0].target)
        assertEquals("set_temperature", result[0].action)
        assertEquals("26", result[0].parameters["temperature"])
    }

    @Test
    fun `Given 多个 function-call When parse Then 返回多个 VehicleControl`() {
        val json = """
            {
                "calls": [
                    {
                        "function": "window_control",
                        "arguments": { "action": "close", "position": "driver" }
                    },
                    {
                        "function": "sunroof_control",
                        "arguments": { "action": "open" }
                    }
                ]
            }
        """.trimIndent()

        val result = parser.parse(json)

        assertEquals(2, result.size)
        assertEquals(ControlTarget.WINDOW, result[0].target)
        assertEquals(ControlTarget.SUNROOF, result[1].target)
    }

    @Test
    fun `Given ac_control with all params When parse Then 参数完整传递`() {
        val json = """
            {
                "calls": [
                    {
                        "function": "ac_control",
                        "arguments": {
                            "action": "set_temperature",
                            "temperature": "22",
                            "mode": "auto",
                            "fan_speed": "3"
                        }
                    }
                ]
            }
        """.trimIndent()

        val result = parser.parse(json)

        assertEquals(1, result.size)
        assertEquals("22", result[0].parameters["temperature"])
        assertEquals("auto", result[0].parameters["mode"])
        assertEquals("3", result[0].parameters["fan_speed"])
    }

    // ── 所有 function 名称映射 ──────────────────────────────────

    @Test
    fun `Given function 名称为已知车控映射 When parse Then 正确映射到 ControlTarget`() {
        val mappings = mapOf(
            "ac_control" to ControlTarget.AC,
            "window_control" to ControlTarget.WINDOW,
            "sunroof_control" to ControlTarget.SUNROOF,
            "seat_control" to ControlTarget.SEAT,
            "light_control" to ControlTarget.LIGHT,
            "door_lock_control" to ControlTarget.DOOR_LOCK,
            "trunk_control" to ControlTarget.TRUNK,
            "horn_control" to ControlTarget.HORN,
            "wiper_control" to ControlTarget.WIPER,
            "mirror_control" to ControlTarget.MIRROR,
        )

        mappings.forEach { (functionName, expectedTarget) ->
            val json = """
                {"calls": [{"function": "$functionName", "arguments": {"action": "test"}}]}
            """.trimIndent()

            val result = parser.parse(json)

            assertEquals("function=$functionName", 1, result.size)
            assertEquals("function=$functionName", expectedTarget, result[0].target)
        }
    }

    // ── 边界条件 ───────────────────────────────────────────────

    @Test
    fun `Given 空 JSON 字符串 When parse Then 返回空列表`() {
        val result = parser.parse("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given 空白 JSON When parse Then 返回空列表`() {
        val result = parser.parse("   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given 非法 JSON When parse Then 返回空列表不抛异常`() {
        val result = parser.parse("not a json")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given calls 数组为空 When parse Then 返回空列表`() {
        val json = """{"calls": []}"""
        val result = parser.parse(json)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given function 名称为未知映射 When parse Then 跳过该 call`() {
        val json = """
            {
                "calls": [
                    { "function": "unknown_function", "arguments": { "action": "test" } },
                    { "function": "ac_control", "arguments": { "action": "set_temperature", "temperature": "20" } }
                ]
            }
        """.trimIndent()

        val result = parser.parse(json)

        // 只有已知映射的 ac_control 被解析，unknown_function 被跳过
        assertEquals(1, result.size)
        assertEquals(ControlTarget.AC, result[0].target)
    }

    @Test
    fun `Given arguments 为空 When parse Then parameters 为空 map`() {
        val json = """
            {"calls": [{"function": "horn_control", "arguments": {}}]}
        """.trimIndent()

        val result = parser.parse(json)

        assertEquals(1, result.size)
        assertTrue(result[0].parameters.isEmpty())
    }

    @Test
    fun `Given 缺少 calls 字段 When parse Then 返回空列表`() {
        val json = """{"something": "else"}"""
        val result = parser.parse(json)
        assertTrue(result.isEmpty())
    }
}
