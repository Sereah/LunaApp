package com.lunacattus.voice.vehiclecontrol.adapter.manufacturer

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlResult
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlState
import com.lunacattus.voice.vehiclecontrol.domain.model.VehicleControl
import com.lunacattus.voice.vehiclecontrol.domain.port.VehicleControlAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * 默认车厂车控适配器（模拟实现）。
 *
 * 模拟车控命令下发的完整状态流转：
 * EXECUTING → SUCCESS（正常）或 CANCELLED（被取消）。
 *
 * 可作为其他车厂适配器的参考实现或用于集成测试。
 *
 * @param executionDelayMs 模拟执行耗时（毫秒），默认 100ms
 */
class DefaultVehicleControlAdapter(
    private val executionDelayMs: Long = 100L,
) : VehicleControlAdapter {

    override val manufacturerId: String = "default"

    private val cancellationTokens = ConcurrentHashMap<String, Boolean>()

    override fun execute(control: VehicleControl): Flow<ControlResult> = flow {
        cancellationTokens[control.id] = false

        // 发射 EXECUTING
        emit(ControlResult(control.id, ControlState.EXECUTING))

        // 模拟异步执行（可取消）
        val pollInterval = 50L
        var elapsed = 0L
        while (elapsed < executionDelayMs) {
            if (cancellationTokens[control.id] == true) {
                emit(ControlResult(control.id, ControlState.CANCELLED, "已取消"))
                return@flow
            }
            delay(minOf(pollInterval, executionDelayMs - elapsed))
            elapsed += pollInterval
        }

        // 最终检查取消状态
        if (cancellationTokens[control.id] == true) {
            emit(ControlResult(control.id, ControlState.CANCELLED, "已取消"))
            return@flow
        }

        // 发射 SUCCESS
        emit(
            ControlResult(
                control.id,
                ControlState.SUCCESS,
                "执行成功: ${control.target} ${control.action}",
            )
        )
    }

    override fun cancel(controlId: String) {
        cancellationTokens[controlId] = true
    }

    override fun isAvailable(): Boolean = true
}
