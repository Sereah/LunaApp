package com.lunacattus.conflux

import android.app.Application
import com.lunacattus.common.CommonLog
import com.lunacattus.llm.api.LlmSdk
import com.lunacattus.logger.Logger
import com.lunacattus.network.NetworkLog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.initBaseTag("ConfluxApp")
        CommonLog.setLogger(
            debug = { tag, msg ->
                Logger.d(tag, msg)
            },
            error = { tag, msg, tr ->
                Logger.e(tag, msg + tr)
            }
        )
        NetworkLog.setLogger(
            debug = { tag, msg ->
                Logger.d(tag, msg)
            },
            error = { tag, msg, tr ->
                Logger.e(tag, msg + tr)
            }
        )
        LlmSdk.initialize(this)
    }
}