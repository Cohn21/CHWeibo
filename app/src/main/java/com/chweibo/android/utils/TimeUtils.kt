package com.chweibo.android.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    private val weiboDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
    private val displayDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val displayYearFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        weiboDateFormat.timeZone = TimeZone.getTimeZone("GMT")
    }

    fun formatTime(createdAt: String): String {
        return try {
            val date = weiboDateFormat.parse(createdAt) ?: return createdAt
            val now = Date()
            val diff = now.time - date.time

            when {
                diff < 60_000 -> "刚刚"
                diff < 3_600_000 -> "${diff / 60_000}分钟前"
                diff < 86_400_000 -> "${diff / 3_600_000}小时前"
                isSameYear(date, now) -> displayDateFormat.format(date)
                else -> displayYearFormat.format(date)
            }
        } catch (e: Exception) {
            createdAt
        }
    }

    fun formatTimeLong(createdAt: String): String {
        return try {
            val date = weiboDateFormat.parse(createdAt) ?: return createdAt
            displayDateFormat.format(date)
        } catch (e: Exception) {
            createdAt
        }
    }

    private fun isSameYear(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    fun isToday(date: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date }
        val cal2 = Calendar.getInstance().apply { time = Date() }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
