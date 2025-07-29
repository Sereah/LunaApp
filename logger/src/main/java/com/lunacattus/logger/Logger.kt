package com.lunacattus.logger

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private var baseTag: String = "LunaApp"
    private val dateFormat get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun initBaseTag(tag: String) {
        baseTag = tag
    }

    fun d(tag: String = "", message: String) {
        log(message, LogLevel.DEBUG, tag)
    }

    fun i(tag: String = "", message: String) {
        log(message, LogLevel.INFO, tag)
    }

    fun e(tag: String = "", message: String) {
        log(message, LogLevel.ERROR, tag)
    }

    private fun log(message: String, level: LogLevel = LogLevel.INFO, tag: String) {
        val timestamp = dateFormat.format(Date())
        val threadName = Thread.currentThread().name
        val fullTag = "$baseTag [$timestamp] [$threadName] " + if (tag.isNotEmpty()) "[$tag]" else ""

        when (level) {
            LogLevel.INFO -> Log.i(fullTag, message)
            LogLevel.WARN -> Log.w(fullTag, message)
            LogLevel.ERROR -> Log.e(fullTag, message)
            LogLevel.DEBUG -> Log.d(fullTag, message)
            LogLevel.VERBOSE -> Log.v(fullTag, message)
        }
    }

    private enum class LogLevel {
        INFO, WARN, ERROR, DEBUG, VERBOSE
    }
}