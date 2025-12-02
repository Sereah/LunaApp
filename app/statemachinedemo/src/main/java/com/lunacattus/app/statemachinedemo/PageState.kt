package com.lunacattus.app.statemachinedemo

import android.os.Message
import com.lunacattus.common.util.State
import com.lunacattus.common.util.StateMachine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PageState @Inject constructor() : StateMachine(TAG) {

    private val appState = App()
    private val awakeState = Awake()
    private val screenOnState = ScreenOn()
    private val shuttingDownState = ShuttingDown()

    init {
        addState(appState)
        addState(awakeState, appState)
        addState(screenOnState, awakeState)
        addState(shuttingDownState, appState)
        setInitialState(screenOnState)
    }

    override fun onQuitting() {
        super.onQuitting()
    }

    private inner class App : State() {
        override fun enter() {
            super.enter()
        }

        override fun exit() {
            super.exit()
        }

        override fun processMessage(msg: Message?): Boolean {
            return super.processMessage(msg)
        }

        override fun getName(): String? {
            return "AppState"
        }
    }

    private inner class Awake : State() {
        override fun enter() {
            super.enter()
        }

        override fun exit() {
            super.exit()
        }

        override fun processMessage(msg: Message?): Boolean {
            return super.processMessage(msg)
        }

        override fun getName(): String? {
            return "AwakeState"
        }
    }

    private inner class ScreenOn : State() {
        override fun enter() {
            super.enter()
        }

        override fun exit() {
            super.exit()
        }

        override fun processMessage(msg: Message?): Boolean {
            return when (msg?.what) {
                MSG_GO_TO_SLEEP -> {
                    transitionTo(shuttingDownState)
                    true
                }

                else -> false
            }
        }

        override fun getName(): String? {
            return "ScreenOnState"
        }
    }

    private inner class ShuttingDown : State() {
        override fun enter() {
            super.enter()
        }

        override fun exit() {
            super.exit()
        }

        override fun processMessage(msg: Message?): Boolean {
            return super.processMessage(msg)
        }

        override fun getName(): String? {
            return "ShuttingDownState"
        }
    }

    companion object {
        const val TAG = "PageStateMachine"
        const val MSG_GO_TO_SLEEP = 1000
    }
}