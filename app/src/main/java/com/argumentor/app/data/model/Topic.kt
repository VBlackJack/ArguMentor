package com.argumentor.app.data.model

import androidx.room.Entity
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
 */
@Entity(tableName = "topics")
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
