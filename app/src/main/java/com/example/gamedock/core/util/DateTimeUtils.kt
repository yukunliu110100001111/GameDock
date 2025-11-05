package com.example.gamedock.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared helpers for formatting time-sensitive deal information.
 */
object DateTimeUtils {
    fun formatTimestamp(timestampMillis: Long?, pattern: String = "MMM dd, yyyy"): String {
        if (timestampMillis == null) return ""
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(timestampMillis))
    }
}
