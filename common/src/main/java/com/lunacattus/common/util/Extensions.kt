package com.lunacattus.common.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
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

/**
 * 将文件大小（字节）转换为易读的字符串格式
 */
fun Long.toFileSizeString(): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    val tb = gb * 1024

    return when {
        this < kb -> "$this B"
        this < mb -> "%.2f KB".format(this / kb)
        this < gb -> "%.2f MB".format(this / mb)
        this < tb -> "%.2f GB".format(this / gb)
        else -> "%.2f TB".format(this / tb)
    }
}

/**
 * 将时间戳转换为“智能”日期字符串
 * - 今天、昨天、前天
 * - 今年之内：M月d日
 * - 其他年份：yyyy年M月d日
 */
fun Long.toSmartDateString(
    todayString: String,
    yesterdayString: String,
    dayBeforeYesterdayString: String,
    locale: Locale = Locale.getDefault()
): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = this@toSmartDateString }

    val nowYear = now.get(Calendar.YEAR)
    val targetYear = target.get(Calendar.YEAR)

    fun Calendar.isSameDay(other: Calendar): Boolean {
        return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val dayBeforeYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }

    return when {
        target.isSameDay(now) -> todayString
        target.isSameDay(yesterday) -> yesterdayString
        target.isSameDay(dayBeforeYesterday) -> dayBeforeYesterdayString
        nowYear == targetYear -> {
            val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMd")
            SimpleDateFormat(pattern, locale).format(target.time)
        }
        else -> {
            val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyyMMMd")
            SimpleDateFormat(pattern, locale).format(target.time)
        }
    }
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
