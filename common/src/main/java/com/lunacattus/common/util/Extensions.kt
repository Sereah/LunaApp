package com.lunacattus.common.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Long 扩展函数：时间戳转日期时间字符串
 * @param pattern 日期格式，默认 yyyy-MM-dd HH:mm:ss
 * @param timeZone 时区
 * @param locale 格式
 */
fun Long.toDateTimeString(
    pattern: String = "yyyy-MM-dd HH:mm:ss",
    timeZone: TimeZone = TimeZone.getDefault(),
    locale: Locale = Locale.getDefault()
): String {
    val millis = this
    val sdf = SimpleDateFormat(pattern, locale)
    sdf.timeZone = timeZone
    return sdf.format(Date(millis))
}

fun Long.toDuration(): String {
    val ms = this % 1000
    val totalSeconds = this / 1000
    val s = totalSeconds % 60
    val totalMinutes = totalSeconds / 60
    val m = totalMinutes % 60
    val h = totalMinutes / 60

    return "%02d:%02d:%02d.%03d".format(h, m, s, ms)
}

fun Long.toDurationStringShort(): String {
    return when {
        this < 1_000 -> "${this}ms"
        this < 60_000 -> "%.2fs".format(this / 1000f)
        else -> {
            val minutes = this / 60_000
            val seconds = (this % 60_000) / 1000
            "${minutes}m ${seconds}s"
        }
    }
}

fun Float.dpToPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    )
}

inline fun View.setOnClickListenerWithDebounce(
    debounceTime: Long = 500,
    crossinline action: (View) -> Unit
) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            action(it)
        }
    }
}
