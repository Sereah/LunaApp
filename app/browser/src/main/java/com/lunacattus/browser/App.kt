package com.lunacattus.browser

import android.app.Application
import me.jessyan.autosize.AutoSizeConfig

class App: Application() {
    override fun onCreate() {
        super.onCreate()
//        AutoSizeConfig.getInstance()
//            .setBaseOnWidth(true)
//            .setExcludeFontScale(true)
//            .setUseDeviceSize(true)
//            .setCustomFragment(true)
    }
}