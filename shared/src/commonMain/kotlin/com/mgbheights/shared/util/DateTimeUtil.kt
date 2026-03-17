package com.mgbheights.shared.util

import kotlinx.datetime.*

object DateTimeUtil {

    fun now(): Long = Clock.System.now().toEpochMilliseconds()

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "Date not available"
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/" +
                "${localDateTime.monthNumber.toString().padStart(2, '0')}/" +
                "${localDateTime.year}"
    }

    fun formatTime(timestamp: Long): String {
        if (timestamp <= 0) return "--:--"
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return "$displayHour:$minute $amPm"
    }

    fun formatDateTime(timestamp: Long): String {
        if (timestamp <= 0) return "Not available"
        return "${formatDate(timestamp)} ${formatTime(timestamp)}"
    }

    fun formatMonthYear(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${months[localDateTime.monthNumber - 1]} ${localDateTime.year}"
    }

    fun currentMonthKey(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.monthNumber.toString().padStart(2, '0')}"
    }

    fun isOverdue(dueDate: Long): Boolean {
        if (dueDate <= 0) return false
        return Clock.System.now().toEpochMilliseconds() > dueDate
    }

    fun daysBetween(from: Long, to: Long): Int {
        val fromDate = Instant.fromEpochMilliseconds(from)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val toDate = Instant.fromEpochMilliseconds(to)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        return fromDate.daysUntil(toDate)
    }

    fun getRelativeTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val now = Clock.System.now().toEpochMilliseconds()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            days < 30 -> "${days / 7}w ago"
            days < 365 -> "${days / 30}mo ago"
            else -> "${days / 365}y ago"
        }
    }
}
