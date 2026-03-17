package com.mgbheights.android.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    // Full date + time: "15 Jan 2025, 03:30 PM"
    fun formatDateTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    // Date only: "15 Jan 2025"
    fun formatDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    // Time only: "03:30 PM"
    fun formatTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    // Document key for attendance: "2025-01-15"
    fun todayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Due date label: "Due: 31 Jan 2025"
    fun formatDueDate(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return "Due: ${sdf.format(timestamp.toDate())}"
    }
}
