package com.saveit.app.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Unknown size"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            bytes / Math.pow(1024.0, digitGroups.toDouble())
        ) + " " + units[digitGroups.coerceIn(0, units.size - 1)]
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun isToday(timestamp: Long): Boolean {
        val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return todayFormat.format(Date(timestamp)) == todayFormat.format(Date())
    }

    fun isYesterday(timestamp: Long): Boolean {
        val yesterdayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val yesterday = Date(System.currentTimeMillis() - 86400000)
        return yesterdayFormat.format(Date(timestamp)) == yesterdayFormat.format(yesterday)
    }

    fun getRelativeDate(timestamp: Long): String {
        return when {
            isToday(timestamp) -> "Today"
            isYesterday(timestamp) -> "Yesterday"
            else -> formatDate(timestamp)
        }
    }
}
