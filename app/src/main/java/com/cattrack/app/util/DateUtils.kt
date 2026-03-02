package com.cattrack.app.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    // SimpleDateFormat is NOT thread-safe; use ThreadLocal to avoid concurrent access issues
    private val dateFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
    private val displayDateFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("MM月dd日", Locale.getDefault())
    }
    private val timeFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    private val fullTimeFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    fun getTodayDateString(): String = dateFormat.get()!!.format(Date())

    fun formatDate(timestamp: Long): String = dateFormat.get()!!.format(Date(timestamp))

    fun formatDisplayDate(timestamp: Long): String = displayDateFormat.get()!!.format(Date(timestamp))

    fun formatTime(timestamp: Long): String = timeFormat.get()!!.format(Date(timestamp))

    fun formatFullTime(timestamp: Long): String = fullTimeFormat.get()!!.format(Date(timestamp))

    fun getHourFromTimestamp(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.HOUR_OF_DAY)
    }

    fun getThisWeekRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = dateFormat.get()!!.format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = dateFormat.get()!!.format(cal.time)
        return Pair(start, end)
    }

    fun getLastWeekRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = dateFormat.get()!!.format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = dateFormat.get()!!.format(cal.time)
        return Pair(start, end)
    }

    fun getThisMonthRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = dateFormat.get()!!.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = dateFormat.get()!!.format(cal.time)
        return Pair(start, end)
    }

    fun getLastMonthRange(): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = dateFormat.get()!!.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = dateFormat.get()!!.format(cal.time)
        return Pair(start, end)
    }

    fun getLastNDays(n: Int): Pair<String, String> {
        val cal = Calendar.getInstance()
        val end = dateFormat.get()!!.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -(n - 1))
        val start = dateFormat.get()!!.format(cal.time)
        return Pair(start, end)
    }

    fun minutesToHoursMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}min" else "${m}min"
    }

    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "刚刚"
            diff < 3_600_000 -> "${diff / 60_000}分钟前"
            diff < 86_400_000 -> "${diff / 3_600_000}小时前"
            else -> formatDisplayDate(timestamp)
        }
    }

    fun getDayLabel(dateStr: String): String {
        return try {
            val date = dateFormat.get()!!.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val days = arrayOf("日", "一", "二", "三", "四", "五", "六")
            "周${days[cal.get(Calendar.DAY_OF_WEEK) - 1]}"
        } catch (e: Exception) {
            dateStr
        }
    }
}
