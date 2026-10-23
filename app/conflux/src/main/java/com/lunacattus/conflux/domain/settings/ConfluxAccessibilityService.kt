package com.lunacattus.conflux.domain.settings

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.lunacattus.conflux.R
import com.lunacattus.common.coroutine.SafeCoroutine.cancelSafe
import com.lunacattus.common.coroutine.SafeCoroutine.launchSafe
import com.lunacattus.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class ConfluxAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "ConfluxAccessibilityService"
        private const val NOTIFICATION_CHANNEL_ID = "accessibility_service"
        private const val NOTIFICATION_ID = 1001
        private val CLICK_HOT_WORDS = listOf("跳过广告", "跳过")
    }

    private var scanJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(1))
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastTexts = emptyList<String>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        Logger.d(TAG, "onCreate, foreground service started")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Logger.d(TAG, "onServiceConnected")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        scanJob?.cancel()
        scanJob = scope.launchSafe(TAG) {
            val rootNode = rootInActiveWindow ?: return@launchSafe

            val texts = mutableListOf<String>()
            scan(rootNode, texts)

            if (lastTexts != texts) {
                lastTexts = texts
                Logger.d(TAG, "scan package=${event?.packageName}, texts=$lastTexts")
            }

        }
    }

    override fun onInterrupt() {
        Logger.w(TAG, "onInterrupt")
    }

    override fun onDestroy() {
        Logger.e(TAG, "onDestroy")
        scanJob?.cancel()
        scope.cancelSafe()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.accessibility_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.accessibility_notification_channel_description)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle(getString(R.string.accessibility_notification_title))
            .setContentText(getString(R.string.accessibility_notification_content))
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .apply {
                pendingIntent?.let(::setContentIntent)
            }
            .build()
    }

    private fun scan(
        node: AccessibilityNodeInfo,
        texts: MutableList<String>
    ) {
        node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { nodeText ->
            texts.add(nodeText)
            CLICK_HOT_WORDS.firstOrNull(nodeText::contains)?.let { hotWord ->
                val success = nodeClick(node, nodeText, hotWord)
                Logger.d(TAG, "click ${node.packageName} hot word: $hotWord, nodeText=$nodeText, success=$success")
            }
        }

        for (index in 0 until node.childCount) {
            val childNode = node.getChild(index) ?: continue
            scan(childNode, texts)
        }
    }

    private fun nodeClick(node: AccessibilityNodeInfo, nodeText: String, hotWord: String): Boolean {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (bounds.isEmpty) {
            Logger.d(TAG, "gesture click skipped: empty bounds, hot word=$hotWord")
            return false
        }

        val clickX = bounds.exactCenterX()
        val clickY = bounds.exactCenterY()
        val path = Path().apply { moveTo(clickX, clickY) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 50L))
            .build()

        return dispatchGesture(
            gesture,
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    Logger.d(TAG, "gesture click completed: $hotWord, text=$nodeText")
                    showClickFeedback(nodeText)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Logger.d(TAG, "gesture click cancelled: $hotWord, text=$nodeText")
                }
            },
            mainHandler
        )
    }

    private fun showClickFeedback(nodeText: String) {
//        Toast.makeText(this, "点击: $nodeText", Toast.LENGTH_SHORT).show()
    }
}
