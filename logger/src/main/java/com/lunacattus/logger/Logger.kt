package com.lunacattus.logger

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private var baseTag: String = "LunaApp"
    private var showThread = false;
    private val dateFormat get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun initBaseTag(tag: String, showThread: Boolean = true) {
        baseTag = tag
        this.showThread = showThread;
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

    fun box(
        tag: String = "",
        message: String,
        borderChar: Char = '='
    ) {
        val lines = message.split("\n")
        val padding = 2
        val maxLen = lines.maxOf { it.length }
        val contentWidth = maxLen + padding * 2
        val border = borderChar.toString().repeat(contentWidth + 4)

        val boxed = buildString {
            appendLine()
            appendLine(border)
            appendLine(" ".repeat(contentWidth + 2))
            for (line in lines) {
                val padded = " ".repeat(padding) + line.padEnd(maxLen) + " ".repeat(padding)
                appendLine("  $padded  ")
            }
            appendLine(" ".repeat(contentWidth + 2))
            append(border)
            appendLine()
        }

        log(boxed, LogLevel.INFO, tag)
    }

    fun getArray(bytes: ByteArray, offset: Int, length: Int): String {
        val sb = StringBuilder()
        for (i in offset..<length) {
            var tmp = Integer.toHexString(255 and bytes[i].toInt())
            if (tmp.length == 1) {
                tmp = "0$tmp"
            }
            sb.append("$tmp ")
        }
        return sb.toString()
    }


    private fun log(message: String, level: LogLevel = LogLevel.INFO, tag: String) {
        val timestamp = dateFormat.format(Date())
        val threadName = Thread.currentThread().name
        val fullTag = if (showThread) {
            "$baseTag [$timestamp] [$threadName] " + if (tag.isNotEmpty()) "[$tag]" else ""
        } else {
            "$baseTag [$timestamp] " + if (tag.isNotEmpty()) "[$tag]" else ""
        }

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