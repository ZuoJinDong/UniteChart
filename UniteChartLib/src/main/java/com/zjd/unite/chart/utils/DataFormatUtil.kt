package com.zjd.unite.chart.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * @author ZJD
 * @date 2021/6/3
 * @desc
 **/

/**
 * 是否在同一分钟区间
 */
fun isSameMin(l1: Long, l2: Long, minute: Int = 1): Boolean {
    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = l1
    val dateCalendar = Calendar.getInstance()
    dateCalendar.timeInMillis = l2

    return if (nowCalendar[Calendar.YEAR] == dateCalendar[Calendar.YEAR]
        && nowCalendar[Calendar.MONTH] == dateCalendar[Calendar.MONTH]
        && nowCalendar[Calendar.DATE] == dateCalendar[Calendar.DATE]
        && nowCalendar[Calendar.HOUR_OF_DAY] == dateCalendar[Calendar.HOUR_OF_DAY]) {
        nowCalendar[Calendar.MINUTE] / minute == dateCalendar[Calendar.MINUTE] / minute
    } else false
}

/**
 * 是否在同一小时区间
 */
fun isSameHour(l1: Long, l2: Long, hour: Int = 1): Boolean {

    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = l1
    val dateCalendar = Calendar.getInstance()
    dateCalendar.timeInMillis = l2

    return if (nowCalendar[Calendar.YEAR] == dateCalendar[Calendar.YEAR]
        && nowCalendar[Calendar.MONTH] == dateCalendar[Calendar.MONTH]
        && nowCalendar[Calendar.DATE] == dateCalendar[Calendar.DATE]){
        nowCalendar[Calendar.HOUR_OF_DAY] / hour == dateCalendar[Calendar.HOUR_OF_DAY] / hour
    }else{
        false
    }
}

/**
 * 是否同一天
 */
fun isSameDay(l1: Long, l2: Long): Boolean {
    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = l1
    val dateCalendar = Calendar.getInstance()
    dateCalendar.timeInMillis = l2
    return nowCalendar[Calendar.YEAR] == dateCalendar[Calendar.YEAR]
            && nowCalendar[Calendar.MONTH] == dateCalendar[Calendar.MONTH]
            && nowCalendar[Calendar.DATE] == dateCalendar[Calendar.DATE]
}

const val OneDay = 24*60*60000L

/**
 * 是否同一周
 */
fun isSameWeek(l1: Long, l2: Long): Boolean {
    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = l1
    val dateCalendar = Calendar.getInstance()
    dateCalendar.timeInMillis = l2

    return if(abs(l1 - l2) <= 8 * OneDay){
        nowCalendar[Calendar.WEEK_OF_YEAR] == dateCalendar[Calendar.WEEK_OF_YEAR]
    }else{
        false
    }
}

/**
 * 是否同一月
 */
fun isSameMonth(l1: Long, l2: Long): Boolean {
    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = l1
    val dateCalendar = Calendar.getInstance()
    dateCalendar.timeInMillis = l2
    return nowCalendar[Calendar.YEAR] == dateCalendar[Calendar.YEAR]
            && nowCalendar[Calendar.MONTH] == dateCalendar[Calendar.MONTH]
}

/**
 * 是否同一季
 */
fun isSameSeason(l1: Long, l2: Long): Boolean {
    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = l1
    val dateCalendar = Calendar.getInstance()
    dateCalendar.timeInMillis = l2
    return nowCalendar[Calendar.YEAR] == dateCalendar[Calendar.YEAR]
            && nowCalendar[Calendar.MONTH]/3 == dateCalendar[Calendar.MONTH]/3
}

/**
 * 是否同一年
 */
fun isSameYear(l1: Long, l2: Long): Boolean {
    val nowCalendar = Calendar.getInstance()
    nowCalendar.timeInMillis = l1
    val dateCalendar = Calendar.getInstance()
    dateCalendar.timeInMillis = l2
    return nowCalendar[Calendar.YEAR] == dateCalendar[Calendar.YEAR]
}

/**
 * 是否为本年
 */
fun isCurrentYear(time: Long): Boolean {
    val calendar = Calendar.getInstance()
    val currentYear = calendar[Calendar.YEAR]
    calendar.timeInMillis = time
    return currentYear == calendar[Calendar.YEAR]
}

fun getDate(dateStr: String?, format: SimpleDateFormat): Date? {
    return if ("" != dateStr && dateStr != null) {
        try {
            format.parse(dateStr)
        } catch (var3: ParseException) {
            null
        }
    } else {
        null
    }
}

fun getMinuteDate(dateStr: String?): Date? {
    synchronized(com.sangame.jjhcps.base.util.DateUtil.MINUTE) {
        return com.sangame.jjhcps.base.util.DateUtil.getDate(
            dateStr,
            com.sangame.jjhcps.base.util.DateUtil.MINUTE
        )
    }
}