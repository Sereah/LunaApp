package com.lunacattus.app.statemachinedemo

import android.app.Application
import com.lunacattus.logger.Logger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag("StateMachineDemo")
    }
}
