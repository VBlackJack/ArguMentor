package com.argumentor.app.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Production-grade Timber tree for release builds.
 *
 * Features:
 * - Only logs WARN and ERROR level messages
 * - Captures crash context (last N log entries before crash)
 * - Thread-safe circular buffer for recent logs
 * - Formats stack traces for readability
 * - Ready for integration with crash reporting services (Firebase Crashlytics, Sentry)
 *
 * Usage:
 * - In release builds, this tree captures important errors and warnings
 * - Call getCrashContext() to get recent logs when reporting crashes
 * - Integrate with your preferred crash reporting service in logError()
 */
class ProductionTree : Timber.Tree() {

    companion object {
        /**
         * Maximum number of log entries to keep in the circular buffer.
         * These entries provide context when a crash occurs.
         */
        private const val MAX_LOG_ENTRIES = 50

        /**
         * Maximum length for a single log message to prevent memory issues.
         */
        private const val MAX_MESSAGE_LENGTH = 1000

        /**
         * Date format for log timestamps.
         */
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

        /**
         * Thread-safe queue for recent log entries.
         * Used to provide context when crashes occur.
         */
        private val recentLogs = ConcurrentLinkedQueue<LogEntry>()

        /**
         * Get recent log entries for crash context.
         * Call this when reporting a crash to include recent activity.
         *
         * @return List of recent log entries, oldest first
         */
        fun getCrashContext(): List<LogEntry> {
            return recentLogs.toList()
        }

        /**
         * Get crash context as a formatted string.
         * Useful for including in crash reports.
         *
         * @return Formatted string of recent log entries
         */
        fun getCrashContextString(): String {
            return buildString {
                appendLine("=== Recent Log Context (${recentLogs.size} entries) ===")
                recentLogs.forEach { entry ->
                    appendLine(entry.format())
                }
                appendLine("=== End of Context ===")
            }
        }

        /**
         * Clear the log buffer.
         * Call this after successfully uploading crash reports.
         */
        fun clearContext() {
            recentLogs.clear()
        }
    }

    /**
     * Data class representing a single log entry.
     */
    data class LogEntry(
        val timestamp: Long,
        val priority: Int,
        val tag: String?,
        val message: String,
        val throwable: Throwable?
    ) {
        fun format(): String {
            val time = DATE_FORMAT.format(Date(timestamp))
            val level = priorityToString(priority)
            val tagStr = tag?.let { "[$it]" } ?: ""
            val base = "$time $level $tagStr $message"

            return if (throwable != null) {
                "$base\n${getStackTraceString(throwable)}"
            } else {
                base
            }
        }

        private fun priorityToString(priority: Int): String = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "?"
        }

        private fun getStackTraceString(t: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            t.printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Only log WARN and above in production
        return priority >= Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Truncate message if too long
        val truncatedMessage = if (message.length > MAX_MESSAGE_LENGTH) {
            message.take(MAX_MESSAGE_LENGTH) + "... [truncated]"
        } else {
            message
        }

        // Add to circular buffer for crash context
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            priority = priority,
            tag = tag,
            message = truncatedMessage,
            throwable = t
        )

        recentLogs.add(entry)

        // Maintain circular buffer size
        while (recentLogs.size > MAX_LOG_ENTRIES) {
            recentLogs.poll()
        }

        // Log to Android system log
        when (priority) {
            Log.WARN -> Log.w(tag, truncatedMessage, t)
            Log.ERROR -> {
                Log.e(tag, truncatedMessage, t)
                // Report error to crash service
                logError(tag, truncatedMessage, t)
            }
            Log.ASSERT -> {
                Log.wtf(tag, truncatedMessage, t)
                // Report critical error to crash service
                logError(tag, truncatedMessage, t)
            }
        }
    }

    /**
     * Report an error to Firebase Crashlytics.
     *
     * Sends error information to Firebase Crashlytics for crash analysis:
     * - Sets custom keys for error categorization
     * - Logs the error message for context
     * - Records exceptions for non-fatal crash reports
     *
     * @param tag Log tag for categorization
     * @param message Error message
     * @param throwable Optional exception
     */
    private fun logError(tag: String?, message: String, throwable: Throwable?) {
        try {
            FirebaseCrashlytics.getInstance().apply {
                // Set custom key for error categorization
                setCustomKey("error_tag", tag ?: "unknown")

                // Log the message for context (visible in Crashlytics dashboard)
                log("[$tag] $message")

                // Record the exception if present
                if (throwable != null) {
                    recordException(throwable)
                }
            }
        } catch (e: Exception) {
            // Crashlytics not initialized yet or other error
            // Fail silently to avoid cascading errors
            Log.w("ProductionTree", "Failed to log to Crashlytics: ${e.message}")
        }
    }
}
