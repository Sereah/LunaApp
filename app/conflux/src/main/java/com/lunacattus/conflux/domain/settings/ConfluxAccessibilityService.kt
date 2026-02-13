package com.lunacattus.conflux.domain.settings

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class ConfluxAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "ConfluxAccessibilityService"
        private const val ACTION_CLICK = "action.accessibility.click"
        private const val ACTION_SWIPE = "action.accessibility.swipe"
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_CLICK -> {

                }

                ACTION_SWIPE -> {
                    val x1 = intent.getFloatExtra("x1", 500f)
                    val y1 = intent.getFloatExtra("y1", 1500f)
                    val x2 = intent.getFloatExtra("x2", 500f)
                    val y2 = intent.getFloatExtra("y2", 500f)
                    swipe(x1, y1, x2, y2)
                }
            }
        }

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Logger.d(TAG, "onServiceConnected")
        IntentFilter().apply {
            addAction(ACTION_CLICK)
            addAction(ACTION_SWIPE)
        }.let {
            ContextCompat.registerReceiver(this, receiver, it, ContextCompat.RECEIVER_EXPORTED)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Logger.d(TAG, "onAccessibilityEvent, $event")
    }

    override fun onInterrupt() {
        unregisterReceiver(receiver)
    }

    private fun clickAt(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        dispatchGesture(builder.build(), null, null)
    }

    /**
     * 模拟滑动
     * @param startX 起点X
     * @param startY 起点Y
     * @param endX 终点X
     * @param endY 终点Y
     * @param duration 持续时间（毫秒），建议 300-800ms
     */
    private fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 500) {
        val path = Path()
        path.moveTo(startX, startY) // 移动到起点
        path.lineTo(endX, endY)     // 划线到终点

        val builder = GestureDescription.Builder()
        // StrokeDescription 的参数：路径，开始时间延迟，持续时间
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))

        // 执行手势
        dispatchGesture(builder.build(), object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                // 滑动完成回调
            }
        }, null)
    }
}