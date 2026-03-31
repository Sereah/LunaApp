package com.lunacattus.common.utils

import android.content.Context
import android.text.format.DateFormat
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
            val pattern = DateFormat.getBestDateTimePattern(locale, "MMMd")
            SimpleDateFormat(pattern, locale).format(target.time)
        }
        else -> {
            val pattern = DateFormat.getBestDateTimePattern(locale, "yyyyMMMd")
            SimpleDateFormat(pattern, locale).format(target.time)
        }
    }
}
