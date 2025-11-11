package com.argumentor.app.data.model

import java.time.Instant

/**
 * BUG-010: Standardized timestamp handling using java.time.Instant.
 * This ensures consistency across the entire application and avoids
 * SimpleDateFormat thread-safety issues.
 *
 * Utility function to get current timestamp in ISO 8601 format.
 * Example: "2025-11-08T13:00:00Z"
 */
fun getCurrentIsoTimestamp(): String {
    return Instant.now().toString()
}
