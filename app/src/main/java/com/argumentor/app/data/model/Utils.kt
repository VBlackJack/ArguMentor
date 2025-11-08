package com.argumentor.app.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility function to get current timestamp in ISO 8601 format.
 * Example: "2025-11-08T13:00:00Z"
 */
fun getCurrentIsoTimestamp(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return dateFormat.format(Date())
}
