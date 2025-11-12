package com.argumentor.app.util

import android.content.Context
import com.argumentor.app.R
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for locale-aware formatting of dates, numbers, and other data.
 *
 * INTERNATIONALIZATION: formatRelativeTime now requires Context parameter for string resources.
 */
object FormattingUtils {

    /**
     * Configuration for timestamp validation.
     * These values define the acceptable range for timestamps in the application.
     */
    private object TimestampBounds {
        /**
         * Minimum valid timestamp (epoch 0: January 1, 1970).
         * Timestamps before this are considered invalid.
         */
        const val MIN_TIMESTAMP = 0L

        /**
         * Maximum valid timestamp (January 1, 3000 at 00:00:00 UTC).
         * This prevents overflow and unreasonable future dates.
         * Calculated as milliseconds since epoch for Jan 1, 3000.
         */
        const val MAX_TIMESTAMP = 32503680000000L // ~year 3000
    }

    /**
     * Validate timestamp is within reasonable bounds.
     *
     * ISSUE-001 FIX: Prevents crashes from negative or extremely large timestamps.
     *
     * @param timestamp The timestamp to validate
     * @throws IllegalArgumentException if timestamp is invalid
     */
    private fun validateTimestamp(timestamp: Long) {
        require(timestamp >= TimestampBounds.MIN_TIMESTAMP) {
            "Timestamp cannot be negative: $timestamp"
        }

        require(timestamp <= TimestampBounds.MAX_TIMESTAMP) {
            "Timestamp too large: $timestamp (max: ${TimestampBounds.MAX_TIMESTAMP})"
        }
    }

    /**
     * Format a timestamp to a localized short date string
     * Example: "12/31/2024" (US) or "31/12/2024" (FR)
     */
    fun formatShortDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        validateTimestamp(timestamp)
        val date = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)
        return dateFormat.format(date)
    }

    /**
     * Format a timestamp to a localized medium date string
     * Example: "Dec 31, 2024" (US) or "31 déc. 2024" (FR)
     */
    fun formatMediumDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        validateTimestamp(timestamp)
        val date = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
        return dateFormat.format(date)
    }

    /**
     * Format a timestamp to a localized long date string
     * Example: "December 31, 2024" (US) or "31 décembre 2024" (FR)
     */
    fun formatLongDate(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        validateTimestamp(timestamp)
        val date = Date(timestamp)
        val dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale)
        return dateFormat.format(date)
    }

    /**
     * Format a timestamp to a localized date and time string
     * Example: "12/31/24, 3:30 PM" (US) or "31/12/2024 15:30" (FR)
     */
    fun formatDateTime(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        validateTimestamp(timestamp)
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
     *
     * INTERNATIONALIZATION: Now uses string resources instead of hardcoded FR/EN strings.
     * This is a simplified version - for production consider using
     * androidx.compose.ui.text.android.style.RelativeTimeTextSpan or similar
     *
     * @param context Context to access string resources
     * @param timestamp The timestamp to format
     * @param locale The locale for date formatting fallback (default: device locale)
     */
    fun formatRelativeTime(context: Context, timestamp: Long, locale: Locale = Locale.getDefault()): String {
        validateTimestamp(timestamp)
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> context.getString(R.string.time_just_now)
            diff < 3600_000 -> {
                val minutes = (diff / 60_000).toInt()
                context.getString(R.string.time_minutes_ago, minutes)
            }
            diff < 86400_000 -> {
                val hours = (diff / 3600_000).toInt()
                context.getString(R.string.time_hours_ago, hours)
            }
            diff < 604800_000 -> {
                val days = (diff / 86400_000).toInt()
                context.getString(R.string.time_days_ago, days)
            }
            else -> formatMediumDate(timestamp, locale)
        }
    }
}
