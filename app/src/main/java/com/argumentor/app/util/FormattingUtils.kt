package com.argumentor.app.util

import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for locale-aware formatting of dates, numbers, and other data
 */
object FormattingUtils {

    /**
     * Format a timestamp to a localized short date string
     * Example: "12/31/2024" (US) or "31/12/2024" (FR)
     */
    fun formatShortDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)
        return dateFormat.format(date)
    }

    /**
     * Format a timestamp to a localized medium date string
     * Example: "Dec 31, 2024" (US) or "31 déc. 2024" (FR)
     */
    fun formatMediumDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
        return dateFormat.format(date)
    }

    /**
     * Format a timestamp to a localized long date string
     * Example: "December 31, 2024" (US) or "31 décembre 2024" (FR)
     */
    fun formatLongDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale)
        return dateFormat.format(date)
    }

    /**
     * Format a timestamp to a localized date and time string
     * Example: "12/31/24, 3:30 PM" (US) or "31/12/2024 15:30" (FR)
     */
    fun formatDateTime(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT,
            locale
        )
        return dateFormat.format(date)
    }

    /**
     * Format a number with localized thousands separator
     * Example: "1,234.56" (US) or "1 234,56" (FR)
     */
    fun formatNumber(number: Number, locale: Locale = Locale.getDefault()): String {
        val numberFormat = NumberFormat.getNumberInstance(locale)
        return numberFormat.format(number)
    }

    /**
     * Format a number as percentage with localized format
     * Example: "75%" (US) or "75 %" (FR)
     */
    fun formatPercent(value: Double, locale: Locale = Locale.getDefault()): String {
        val percentFormat = NumberFormat.getPercentInstance(locale)
        return percentFormat.format(value)
    }

    /**
     * Format a relative time string (e.g., "2 hours ago", "3 days ago")
     * This is a simplified version - for production consider using
     * androidx.compose.ui.text.android.style.RelativeTimeTextSpan or similar
     */
    fun formatRelativeTime(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> if (locale.language == "fr") "À l'instant" else "Just now"
            diff < 3600_000 -> {
                val minutes = (diff / 60_000).toInt()
                if (locale.language == "fr") {
                    "Il y a $minutes minute${if (minutes > 1) "s" else ""}"
                } else {
                    "$minutes minute${if (minutes > 1) "s" else ""} ago"
                }
            }
            diff < 86400_000 -> {
                val hours = (diff / 3600_000).toInt()
                if (locale.language == "fr") {
                    "Il y a $hours heure${if (hours > 1) "s" else ""}"
                } else {
                    "$hours hour${if (hours > 1) "s" else ""} ago"
                }
            }
            diff < 604800_000 -> {
                val days = (diff / 86400_000).toInt()
                if (locale.language == "fr") {
                    "Il y a $days jour${if (days > 1) "s" else ""}"
                } else {
                    "$days day${if (days > 1) "s" else ""} ago"
                }
            }
            else -> formatMediumDate(timestamp, locale)
        }
    }
}
