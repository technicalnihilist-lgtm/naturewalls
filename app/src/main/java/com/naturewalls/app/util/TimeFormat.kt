package com.naturewalls.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormat {

    /** "just now", "4 min ago", "3 h ago", "2 d ago" — used by the live badge. */
    fun relative(timestamp: Long): String {
        if (timestamp <= 0L) return "not yet"
        val delta = System.currentTimeMillis() - timestamp
        if (delta < 0) return "just now"
        val minutes = TimeUnit.MILLISECONDS.toMinutes(delta)
        val hours = TimeUnit.MILLISECONDS.toHours(delta)
        val days = TimeUnit.MILLISECONDS.toDays(delta)
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours h ago"
            days < 7 -> "$days d ago"
            else -> formatDate(timestamp)
        }
    }

    fun formatDate(timestamp: Long): String =
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))

    /** Commons timestamps arrive as "2024-05-06 12:34:56". */
    fun commonsDate(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val parsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(raw.take(19))
            parsed?.let { SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(it) }
        }.getOrNull()
    }

    fun todayLabel(): String =
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
}
