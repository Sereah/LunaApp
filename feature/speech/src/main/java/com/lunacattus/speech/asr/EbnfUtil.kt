package com.lunacattus.speech.asr

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object EbnfUtil {

    fun generateEbnfFromAssets(
        context: Context,
        assetFileName: String,
        replacements: Map<String, String>
    ): String {
        val sb = StringBuilder()
        context.assets.open(assetFileName).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    var newLine = line!!
                    // 遍历所有占位符，逐个替换
                    for ((holder, value) in replacements) {
                        newLine = newLine.replace(holder, value)
                    }
                    sb.append(newLine).append("\n")
                }
            }
        }
        return sb.toString()
    }

}