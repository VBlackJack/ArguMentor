package com.argumentor.app.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Source entity representing bibliographic sources.
 *
 * @property id Unique identifier (UUID v4)
 * @property title Source title
 * @property citation Full citation (author, edition, year)
 * @property url Optional URL
 * @property publisher Optional publisher name
 * @property date Optional publication date
 * @property reliabilityScore Optional reliability score (0.0-1.0)
 * @property notes Additional notes about the source
 * @property createdAt Creation timestamp in ISO 8601 format
 * @property updatedAt Last update timestamp in ISO 8601 format
 *
 * Note on Validation Strategy:
 * This entity validates `reliabilityScore` in the init block because:
 * 1. It's a CONSTRAINT that must ALWAYS hold (mathematical bound)
 * 2. Invalid values would cause logical errors downstream
 * 3. It's a data integrity issue, not a business rule
 *
 * Other entities don't have init validation because:
 * - Most validation is CONTEXTUAL (e.g., "empty title") and belongs in ViewModels
 * - ViewModels validate before save, showing user-friendly error messages
 * - Database constraints (NOT NULL, etc.) handle the rest
 * - Only invariants that must NEVER be violated belong in entity init blocks
 *
 * This keeps entities lightweight while ensuring critical invariants are enforced.
 *
 * QUALITY-001: @Immutable annotation helps Compose skip recomposition when data hasn't changed.
 * This data class is immutable (all properties are val) and contains only immutable types.
 */
@Immutable
@Entity(tableName = "sources")
data class Source(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val citation: String? = null,
    val url: String? = null,
    val publisher: String? = null,
    val date: String? = null,
    val reliabilityScore: Double? = null,
    val notes: String? = null,
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
) {
    init {
        reliabilityScore?.let { score ->
            require(score in 0.0..1.0) {
                "reliabilityScore must be between 0.0 and 1.0, got $score"
            }
        }
    }
}
