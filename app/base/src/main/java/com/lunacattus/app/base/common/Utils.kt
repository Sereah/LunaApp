package com.lunacattus.app.base.common

import android.content.Context
import android.util.TypedValue
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

fun Float.dpToPx(context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    )
}
