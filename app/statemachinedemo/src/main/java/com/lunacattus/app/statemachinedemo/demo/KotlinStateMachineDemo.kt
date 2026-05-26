package com.lunacattus.app.statemachinedemo.demo

import android.util.Log
import com.lunacattus.statemachine.sample.SampleStateMachine
import com.lunacattus.statemachine.sample.ISampleState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Kotlin Channel 版 [SampleStateMachine] 的使用示例。
 *
 * 模拟一个音乐播放器状态机：
 * ```
 * Idle ──→ Playing ←──→ Paused
 * ```
 *
 * 另外演示 deferEvent 机制：
 * Connecting 下的事件会延期到 Connected 后重新处理。
 */
object KotlinStateMachineDemo {

    fun run(onLog: (String) -> Unit) {
        val logger: (String) -> Unit = { msg ->
            Log.d("KotlinSM", msg)
            onLog(msg)
        }

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        logger("=== Kotlin Channel 状态机 Demo ===")
        runMusicPlayerDemo(scope, logger)
        runDeferEventDemo(scope, logger)

        scope.cancel()
        logger("=== Kotlin Demo 结束 ===")
    }

    private fun runBlockingWithDelay(ms: Long) {
        try { Thread.sleep(ms) } catch (_: InterruptedException) { }
    }

    // ==================== 音乐播放器 Demo ====================

    private sealed interface MusicEvent {
        data object Play : MusicEvent
        data object Pause : MusicEvent
        data object Stop : MusicEvent
    }

    private class MusicPlayerStateMachine(
        scope: CoroutineScope,
        private val onLog: (String) -> Unit
    ) : SampleStateMachine<MusicEvent>(scope, "MusicSM") {

        init {
            addState(IdleState(this, onLog))
            addState(PlayingState(this, onLog))
            addState(PausedState(this, onLog))
            setInitialState(IdleState(this, onLog))
        }

        override fun onUnhandledEvent(event: MusicEvent, state: ISampleState<MusicEvent>) {
            onLog("[未处理] event=$event, state=${state.name}")
        }

        override fun onQuitting() {
            onLog("MusicPlayerStateMachine 已退出")
        }
    }

    private class IdleState(
        private val machine: SampleStateMachine<MusicEvent>,
        private val onLog: (String) -> Unit
    ) : ISampleState<MusicEvent> {
        override val name = "Idle"

        override suspend fun enter() {
            onLog("IdleState.enter — 空闲中")
        }

        override suspend fun processEvent(event: MusicEvent): Boolean = when (event) {
            is MusicEvent.Play -> {
                onLog("IdleState 处理 Play，切换到 Playing")
                machine.transitionTo(PlayingState(machine, onLog))
                true
            }
            else -> false
        }

        override suspend fun exit() {
            onLog("IdleState.exit")
        }
    }

    private class PlayingState(
        private val machine: SampleStateMachine<MusicEvent>,
        private val onLog: (String) -> Unit
    ) : ISampleState<MusicEvent> {
        override val name = "Playing"

        override suspend fun enter() { onLog("PlayingState.enter — 开始播放") }

        override suspend fun processEvent(event: MusicEvent): Boolean = when (event) {
            is MusicEvent.Pause -> {
                onLog("PlayingState 处理 Pause")
                machine.transitionTo(PausedState(machine, onLog))
                true
            }
            is MusicEvent.Stop -> {
                onLog("PlayingState 处理 Stop")
                machine.transitionTo(IdleState(machine, onLog))
                true
            }
            else -> false
        }

        override suspend fun exit() { onLog("PlayingState.exit — 停止播放") }
    }

    private class PausedState(
        private val machine: SampleStateMachine<MusicEvent>,
        private val onLog: (String) -> Unit
    ) : ISampleState<MusicEvent> {
        override val name = "Paused"

        override suspend fun enter() { onLog("PausedState.enter — 已暂停") }

        override suspend fun processEvent(event: MusicEvent): Boolean = when (event) {
            is MusicEvent.Play -> {
                onLog("PausedState 处理 Play")
                machine.transitionTo(PlayingState(machine, onLog))
                true
            }
            is MusicEvent.Stop -> {
                onLog("PausedState 处理 Stop")
                machine.transitionTo(IdleState(machine, onLog))
                true
            }
            else -> false
        }

        override suspend fun exit() { onLog("PausedState.exit") }
    }

    private fun runMusicPlayerDemo(scope: CoroutineScope, onLog: (String) -> Unit) {
        val machine = MusicPlayerStateMachine(scope, onLog)
        machine.start()

        scope.launch {
            machine.currentState.collect { state ->
                onLog("[观察] 当前状态: ${state?.name}")
            }
        }

        onLog("--- 发送 Play ---")
        machine.sendEvent(MusicEvent.Play)
        runBlockingWithDelay(100)

        onLog("--- 发送 Pause ---")
        machine.sendEvent(MusicEvent.Pause)
        runBlockingWithDelay(100)

        onLog("--- 发送 Play ---")
        machine.sendEvent(MusicEvent.Play)
        runBlockingWithDelay(100)

        onLog("--- 发送 Stop ---")
        machine.sendEvent(MusicEvent.Stop)
        runBlockingWithDelay(150)

        machine.quit()
    }

    // ==================== deferEvent Demo ====================

    private sealed interface ConnEvent {
        data object Connect : ConnEvent
        data object SendData : ConnEvent
        data object Connected : ConnEvent
    }

    /** 所有状态类定义在使用之前 */
    private class IdleConnState : ISampleState<ConnEvent> {
        override val name = "ConnectionIdle"
        override suspend fun enter() {
            Log.d("KotlinSM", "[IdleConnState] 传输完成，连接空闲")
            Log.d("KotlinSM", "[IdleConnState] Demo 流程结束")
        }
        override suspend fun processEvent(event: ConnEvent): Boolean = false
        override suspend fun exit() = Unit
    }

    private class ConnectingState(
        private val machine: SampleStateMachine<ConnEvent>,
        private val onLog: (String) -> Unit
    ) : ISampleState<ConnEvent> {
        override val name = "Connecting"

        override suspend fun enter() { onLog("ConnectingState.enter — 连接中...") }

        override suspend fun processEvent(event: ConnEvent): Boolean = when (event) {
            is ConnEvent.SendData -> {
                onLog("ConnectingState：连接未就绪，defer SendData")
                machine.deferEvent(event)
                true
            }
            is ConnEvent.Connected -> {
                onLog("ConnectingState：连接成功！切换 ConnectedState")
                machine.transitionTo(ConnectedState(machine, onLog))
                true
            }
            else -> false
        }

        override suspend fun exit() { onLog("ConnectingState.exit") }
    }

    private class ConnectedState(
        private val machine: SampleStateMachine<ConnEvent>,
        private val onLog: (String) -> Unit
    ) : ISampleState<ConnEvent> {
        override val name = "Connected"

        override suspend fun enter() { onLog("ConnectedState.enter — 延期事件将自动重新入队处理") }

        override suspend fun processEvent(event: ConnEvent): Boolean = when (event) {
            is ConnEvent.SendData -> {
                onLog("ConnectedState：收到 SendData（原延期事件已到达）")
                machine.transitionTo(IdleConnState())
                true
            }
            else -> false
        }

        override suspend fun exit() { onLog("ConnectedState.exit") }
    }

    private fun runDeferEventDemo(scope: CoroutineScope, logger: (String) -> Unit) {
        logger("")
        logger("=== deferEvent 延期事件 Demo ===")

        val machine = SampleStateMachine<ConnEvent>(scope, "ConnSM")
        machine.addState(ConnectingState(machine, logger))
        machine.addState(ConnectedState(machine, logger))
        machine.addState(IdleConnState())
        machine.setInitialState(ConnectingState(machine, logger))
        machine.start()

        scope.launch {
            machine.currentState.collect { state ->
                logger("[观察] 连接状态: ${state?.name}")
            }
        }

        runBlockingWithDelay(50)
        logger("--- 连接未就绪时发送 SendData（会被 defer）---")
        machine.sendEvent(ConnEvent.SendData)

        runBlockingWithDelay(50)
        logger("--- 发送 Connected 触发切换 + flush deferred---")
        machine.sendEvent(ConnEvent.Connected)

        runBlockingWithDelay(200)
    }
}
