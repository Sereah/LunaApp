package com.lunacattus.voice.vehiclecontrol.domain.port

import com.lunacattus.voice.vehiclecontrol.domain.model.ControlResult
import com.lunacattus.voice.vehiclecontrol.domain.model.ControlTask

/**
 * 出站端口（SPI）：车控状态观察者。
 *
 * 当任务状态或单个操作结果发生变更时通知所有已注册的观察者。
 */
interface ControlStateObserver {

    /**
     * 任务状态变更回调。
     */
    fun onTaskStateChanged(task: ControlTask)

    /**
     * 单个车控操作结果回调。
     */
    fun onControlResult(result: ControlResult)
}
