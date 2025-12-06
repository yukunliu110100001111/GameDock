package com.example.gamedock.data.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    fun formatTimestamp(timestampMillis: Long?, pattern: String = "MMM dd, yyyy"): String {
        // Safely format a timestamp; return empty when null.
        if (timestampMillis == null) return ""
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(timestampMillis))
    }
}
