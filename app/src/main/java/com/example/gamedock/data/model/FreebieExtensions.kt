package com.example.gamedock.data.model

import java.util.*

val Freebie.startDateMillis: Long?
    get() = startDate?.let { parseIsoDate(it) }

val Freebie.endDateMillis: Long?
    get() = endDate?.let { parseIsoDate(it) }

private fun parseIsoDate(date: String): Long? {
    return try {
        val year = date.substring(0, 4).toInt()
        val month = date.substring(5, 7).toInt()
        val day = date.substring(8, 10).toInt()
        val hour = date.substring(11, 13).toInt()
        val minute = date.substring(14, 16).toInt()

        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(year, month - 1, day, hour, minute, 0)
        cal.timeInMillis
    } catch (e: Exception) {
        null
    }
}

val Freebie.remainingText: String
    get() {
        val now = System.currentTimeMillis()
        val end = endDateMillis
        val start = startDateMillis

        if (start != null && now < start) {
            val diff = start - now
            val days = diff / (1000 * 60 * 60 * 24)
            val hours = (diff / (1000 * 60 * 60)) % 24
            return if (days > 0) "Starts in ${days}d ${hours}h"
            else if (hours > 0) "Starts in ${hours}h"
            else "Starting soon"
        }

        if (end != null && now < end) {
            val diff = end - now
            val days = diff / (1000 * 60 * 60 * 24)
            val hours = (diff / (1000 * 60 * 60)) % 24
            return if (days > 0) "Ends in ${days}d ${hours}h"
            else if (hours > 0) "Ends in ${hours}h"
            else "Ending soon"
        }

        return "Ended"
    }

