package com.lunacattus.browser

import android.app.Activity
import android.app.Application
import android.util.Log

/**
 * AutoSize density 修复工具
 *
 * 解决问题：在 GMS Pixel 设备上，AutoSize 库在 Application 初始化时可能缓存错误的屏幕宽度
 * （竖屏 Launcher 的宽度），导致横屏 Activity 的 density 计算错误。
 *
 * 使用方式：在 Activity.onCreate() 中，super.onCreate() 之后、setContentView() 之前调用：
 * ```
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     AutoSizeUtil.fixDensity(this, 1920f)
 *     setContentView(R.layout.activity_main)
 * }
 * ```
 */
object AutoSizeUtil {

    private const val TAG = "AutoSizeUtil"

    /** 是否启用调试日志，正式发布时设为 false */
    var debugLog = true

    /**
     * 修复 density，确保基于正确的屏幕宽度计算。
     * 必须在 setContentView() 之前调用。
     *
     * @param activity 当前 Activity
     * @param designWidthDp 设计稿宽度（dp），例如 1920f
     * @param baseOnWidth 是否基于宽度适配，默认 true
     */
    fun fixDensity(
        activity: Activity,
        designWidthDp: Float,
        baseOnWidth: Boolean = true
    ) {
        val sysMetrics = activity.resources.displayMetrics
        val screenSize = if (baseOnWidth) sysMetrics.widthPixels else sysMetrics.heightPixels
        val expectedDensity = screenSize / designWidthDp

        val isCorrupted = sysMetrics.density != expectedDensity

        if (debugLog) {
            Log.w(TAG, "===== fixDensity 开始 =====")
            Log.w(TAG, "[修复前] density=${sysMetrics.density}, " +
                    "densityDpi=${sysMetrics.densityDpi}, " +
                    "config.densityDpi=${activity.resources.configuration.densityDpi}, " +
                    "屏幕宽=${sysMetrics.widthPixels}px, " +
                    "屏幕高=${sysMetrics.heightPixels}px, " +
                    "基准=${if (baseOnWidth) "宽度" else "高度"}")

            if (isCorrupted) {
                Log.e(TAG, "⚠️ density 被污染！当前=${sysMetrics.density}, 正确应为=$expectedDensity")
            } else {
                Log.w(TAG, "✅ density=$expectedDensity，未被污染")
            }
        }

        // 计算目标值
        val targetDensity = expectedDensity
        val targetDensityDpi = (targetDensity * 160).toInt()

        // 设置 Activity 级别
        sysMetrics.density = targetDensity
        sysMetrics.scaledDensity = targetDensity
        sysMetrics.densityDpi = targetDensityDpi

        // 设置 Application 级别
        val appMetrics = activity.application.resources.displayMetrics
        appMetrics.density = targetDensity
        appMetrics.scaledDensity = targetDensity
        appMetrics.densityDpi = targetDensityDpi

        if (debugLog) {
            Log.w(TAG, "[修复后] density=${sysMetrics.density}, densityDpi=${sysMetrics.densityDpi}")
            Log.w(TAG, "===== fixDensity 完成 =====")
        }
    }
}
