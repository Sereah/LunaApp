package com.lunacattus.voice.vehiclecontrol.domain.port

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlResult
import com.lunacattus.voice.vehiclecontrol.domain.model.VehicleControl
import kotlinx.coroutines.flow.Flow

/**
 * 出站端口（SPI）：车厂车控协议适配器。
 *
 * 不同车厂（比亚迪、特斯拉、蔚来等）有各自的车控协议，通过实现此接口接入。
 * 通过 [manufacturerId] 路由到对应的适配器实例。
 */
interface VehicleControlAdapter {

    /** 车厂标识符，如 "default"、"byd"、"tesla"、"nio" 等 */
    val manufacturerId: String

    /**
     * 执行单个车控操作。
     *
     * @param control 要执行的车控操作
     * @return Flow，依次发射 PENDING → EXECUTING → SUCCESS/FAILED/TIMEOUT
     */
    fun execute(control: VehicleControl): Flow<ControlResult>

    /**
     * 取消正在执行的车控操作。
     */
    fun cancel(controlId: String)

    /**
     * 检查当前适配器是否可用（如车辆连接是否正常）。
     */
    fun isAvailable(): Boolean
}
