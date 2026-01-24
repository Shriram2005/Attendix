package com.windhands.attendix.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility class for date operations
 */
object DateUtils {

    fun getCurrentDate(): Date = Date()

    fun getCurrentDateString(): String {
        val format = SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
        return format.format(Date())
    }

    fun getCurrentTimeString(): String {
        val format = SimpleDateFormat(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault())
        return format.format(Date())
    }

    fun formatDateForApi(date: Date): String {
        val format = SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
        return format.format(date)
    }

    fun formatDateForDisplay(date: Date): String {
        val format = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())
        return format.format(date)
    }

    fun formatDateForDisplayShort(date: Date): String {
        val format = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY_SHORT, Locale.getDefault())
        return format.format(date)
    }

    fun parseApiDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getStartOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getEndOfMonth(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        return calendar.time
    }

    fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getDaysAgo(days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.time
    }

    fun daysBetween(startDate: Date, endDate: Date): Long {
        val diffInMillis = endDate.time - startDate.time
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 1
    }

    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val compare = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == compare.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(date: Date): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val compare = Calendar.getInstance().apply { time = date }
        return yesterday.get(Calendar.YEAR) == compare.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR)
    }

    fun getRelativeDateLabel(dateString: String): String {
        val date = parseApiDate(dateString) ?: return dateString
        return when {
            isToday(date) -> "Today"
            isYesterday(date) -> "Yesterday"
            else -> formatDateForDisplay(date)
        }
    }

    fun getDayOfWeek(date: Date): String {
        val format = SimpleDateFormat("EEEE", Locale.getDefault())
        return format.format(date)
    }

    fun getMonthYear(date: Date): String {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return format.format(date)
    }
}
