package com.lunacattus.app.statemachinedemo

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.lunacattus.app.statemachinedemo.demo.JavaStateMachineDemo
import com.lunacattus.app.statemachinedemo.demo.KotlinStateMachineDemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val logView = TextView(this).apply {
            textSize = 12f
            setLineSpacing(8f, 1f)
            text = "点击下方按钮运行 Demo\n（日志同时输出到 Logcat）\n\n"
        }
        val scrollView = ScrollView(this).apply { addView(logView) }

        val btnJava = Button(this).apply { text = "运行 Java Handler 状态机 Demo" }
        val btnKotlin = Button(this).apply { text = "运行 Kotlin Channel 状态机 Demo" }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(btnJava)
            addView(btnKotlin)
            addView(scrollView)
        }

        btnJava.setOnClickListener {
            logView.text = "Java Handler 状态机 Demo 运行中...\n"
            scope.launch {
                JavaStateMachineDemo.run { line ->
                    runOnUiThread { logView.append("$line\n") }
                }
                runOnUiThread { logView.append("\n=== Java Demo 完成 ===\n") }
            }
        }

        btnKotlin.setOnClickListener {
            logView.text = "Kotlin Channel 状态机 Demo 运行中...\n"
            scope.launch {
                KotlinStateMachineDemo.run { line ->
                    runOnUiThread { logView.append("$line\n") }
                }
                runOnUiThread { logView.append("\n=== Kotlin Demo 完成 ===\n") }
            }
        }

        setContentView(layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
