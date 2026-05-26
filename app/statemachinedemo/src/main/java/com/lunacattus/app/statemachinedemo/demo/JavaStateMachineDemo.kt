package com.lunacattus.app.statemachinedemo.demo

import android.os.Message
import android.util.Log
import com.lunacattus.statemachine.State
import com.lunacattus.statemachine.StateMachine

/**
 * Java Handler 版状态机 [StateMachine] 的使用示例。
 *
 * 模拟一个简化的屏幕亮度状态机，展示分层状态特性：
 *
 * ```
 *              AppState (根：处理 MSG_FINISH)
 *                 |
 *           ActiveState (处理 MSG_SLEEP)
 *           /             \
 * BrightnessHigh     BrightnessLow
 * ```
 *
 * 事件序列：
 *   start → BrightnessHigh
 *   send(MSG_LOW)   → BrightnessLow
 *   send(MSG_HIGH)  → BrightnessHigh
 *   send(MSG_SLEEP) → ActiveState 处理，切换到 BrightnessLow
 *   send(MSG_FINISH)→ AppState 处理，切换到 BrightnessHigh
 *   quit()
 */
object JavaStateMachineDemo {

    private const val MSG_HIGH = 1
    private const val MSG_LOW = 2
    private const val MSG_SLEEP = 3
    private const val MSG_FINISH = 4

    fun run(onLog: (String) -> Unit) {
        val logger: (String) -> Unit = { msg ->
            Log.d("JavaSM", msg)
            onLog(msg)
        }

        logger("=== Java Handler 状态机 Demo ===")

        val sm = ScreenBrightnessMachine(logger)
        sm.start()

        logger("--- send: MSG_LOW ---")
        sm.sendMessage(MSG_LOW)

        Thread.sleep(100)
        logger("--- send: MSG_HIGH ---")
        sm.sendMessage(MSG_HIGH)

        Thread.sleep(100)
        logger("--- send: MSG_SLEEP（由 ActiveState 父状态处理）---")
        sm.sendMessage(MSG_SLEEP)

        Thread.sleep(100)
        logger("--- send: MSG_FINISH（由 AppState 根状态处理）---")
        sm.sendMessage(MSG_FINISH)

        Thread.sleep(100)
        logger("--- quit ---")
        sm.quit()

        logger("=== Java Demo 结束 ===")
    }

    private class ScreenBrightnessMachine(
        private val onLog: (String) -> Unit
    ) : StateMachine("BrightnessSM") {

        // 属性必须在 init 块之前声明，以保证初始化顺序
        private val appState = AppState()
        private val activeState = ActiveState()
        private val highState = BrightnessHigh()
        private val lowState = BrightnessLow()

        init {
            addState(appState)
            addState(activeState, appState)
            addState(highState, activeState)
            addState(lowState, activeState)
            setInitialState(highState)
        }

        // ── 内部状态类（避免 Kotlin object 表达式的初始化顺序问题）──

        private inner class AppState : State() {
            override fun getName() = "AppState"

            override fun enter() {
                onLog("AppState.enter — 系统运行中")
            }

            override fun processMessage(msg: Message): Boolean {
                if (msg.what == MSG_FINISH) {
                    onLog("AppState 捕获 MSG_FINISH，切换到 BrightnessHigh")
                    transitionTo(highState)
                    return HANDLED
                }
                return NOT_HANDLED
            }

            override fun exit() {
                onLog("AppState.exit")
            }
        }

        private inner class ActiveState : State() {
            override fun getName() = "ActiveState"

            override fun enter() {
                onLog("ActiveState.enter — 屏幕活跃")
            }

            override fun processMessage(msg: Message): Boolean {
                if (msg.what == MSG_SLEEP) {
                    onLog("ActiveState 捕获 MSG_SLEEP，切换到 BrightnessLow")
                    transitionTo(lowState)
                    return HANDLED
                }
                return NOT_HANDLED
            }

            override fun exit() {
                onLog("ActiveState.exit")
            }
        }

        private inner class BrightnessHigh : State() {
            override fun getName() = "BrightnessHigh"

            override fun enter() {
                onLog("BrightnessHigh.enter — 亮度 100%")
            }

            override fun processMessage(msg: Message): Boolean {
                if (msg.what == MSG_LOW) {
                    onLog("BrightnessHigh 处理 MSG_LOW")
                    transitionTo(lowState)
                    return HANDLED
                }
                return NOT_HANDLED
            }

            override fun exit() {
                onLog("BrightnessHigh.exit")
            }
        }

        private inner class BrightnessLow : State() {
            override fun getName() = "BrightnessLow"

            override fun enter() {
                onLog("BrightnessLow.enter — 亮度 30%")
            }

            override fun processMessage(msg: Message): Boolean {
                if (msg.what == MSG_HIGH) {
                    onLog("BrightnessLow 处理 MSG_HIGH")
                    transitionTo(highState)
                    return HANDLED
                }
                return NOT_HANDLED
            }

            override fun exit() {
                onLog("BrightnessLow.exit")
            }
        }

        override fun onQuitting() {
            onLog("onQuitting — 状态机退出")
        }
    }
}
