package com.argumentor.app.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.argumentor.app.data.local.Converters
import java.util.UUID

/**
 * Topic entity representing an argumentation subject.
 *
 * @property id Unique identifier (UUID v4)
 * @property title Topic title
 * @property summary Topic summary/description
 * @property posture Tone setting: neutral_critical, skeptical, academic_comparative
 * @property tags List of tag IDs associated with this topic
 * @property createdAt Creation timestamp in ISO 8601 format
 * @property updatedAt Last update timestamp in ISO 8601 format
 *
 * Note on Tag Relationships:
 * The `tags` property stores Tag IDs as a List<String> instead of using a junction table
 * (Topic_Tag) with proper Foreign Keys. This design choice was made because:
 * 1. SIMPLICITY: Easier to query and manage for the common use case (read topics with tags)
 * 2. PERFORMANCE: Avoids JOIN queries on every topic fetch - tags are loaded directly
 * 3. FLEXIBILITY: Easy to add/remove tags without separate table operations
 *
 * Trade-offs:
 * - No automatic CASCADE delete when a tag is removed (orphaned IDs possible)
 * - Manual cleanup needed if tags are deleted (currently not a common operation)
 * - Cannot easily query "all topics for a tag" without filtering in code
 *
 * This is acceptable because:
 * - Tags are rarely deleted in this app
 * - Most queries are "get topics" → "show their tags" (read-heavy)
 * - Tag filtering happens at the ViewModel level which is performant enough
 *
 * QUALITY-001: @Immutable annotation helps Compose skip recomposition when data hasn't changed.
 * This data class is immutable (all properties are val) and contains only immutable types.
 */
@Immutable
@Entity(
    tableName = "topics",
    indices = [Index(value = ["posture"])]
)
@TypeConverters(Converters::class)
data class Topic(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val posture: Posture = Posture.NEUTRAL_CRITICAL,
    val tags: List<String> = emptyList(),
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
) {
    enum class Posture {
        NEUTRAL_CRITICAL,
        SKEPTICAL,
        ACADEMIC_COMPARATIVE;

        companion object {
            fun fromString(value: String): Posture {
                return when (value.lowercase()) {
                    "neutral_critical", "neutral_critique", "neutre_critique" -> NEUTRAL_CRITICAL
                    "skeptical", "sceptique" -> SKEPTICAL
                    "academic_comparative", "comparatif_academique" -> ACADEMIC_COMPARATIVE
                    else -> NEUTRAL_CRITICAL
                }
            }
        }

        override fun toString(): String {
            return when (this) {
                NEUTRAL_CRITICAL -> "neutral_critical"
                SKEPTICAL -> "skeptical"
                ACADEMIC_COMPARATIVE -> "academic_comparative"
            }
        }
    }
}
