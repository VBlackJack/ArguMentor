package com.argumentor.app.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.argumentor.app.data.local.Converters
import java.util.UUID

/**
 * Claim entity representing an affirmation/argument.
 *
 * @property id Unique identifier (UUID v4)
 * @property text The claim text content
 * @property stance Position: pro, con, or neutral
 * @property strength Evidence strength: low, medium, high
 * @property topics List of topic IDs this claim belongs to (many-to-many)
 * @property fallacyIds List of fallacy IDs identified in this claim
 * @property createdAt Creation timestamp in ISO 8601 format
 * @property updatedAt Last update timestamp in ISO 8601 format
 * @property claimFingerprint SHA-256 hash of normalized text for duplicate detection
 *
 * QUALITY-001: @Immutable annotation helps Compose skip recomposition when data hasn't changed.
 * This data class is immutable (all properties are val) and contains only immutable types.
 */
@Immutable
@Entity(
    tableName = "claims",
    indices = [
        Index(value = ["claimFingerprint"], unique = true),
        Index(value = ["stance"]),
        Index(value = ["strength"])
    ]
)
@TypeConverters(Converters::class)
data class Claim(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val stance: Stance = Stance.NEUTRAL,
    val strength: Strength = Strength.MEDIUM,
    val topics: List<String> = emptyList(),
    val fallacyIds: List<String> = emptyList(),
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp(),
    val claimFingerprint: String = ""
) {
    enum class Stance {
        PRO, CON, NEUTRAL;

        companion object {
            fun fromString(value: String): Stance {
                return when (value.lowercase()) {
                    "pro" -> PRO
                    "con" -> CON
                    "neutral" -> NEUTRAL
                    else -> NEUTRAL
                }
            }
        }

        override fun toString(): String {
            return name.lowercase()
        }
    }

    enum class Strength {
        LOW, MEDIUM, HIGH;

        companion object {
            fun fromString(value: String): Strength {
                return when (value.lowercase()) {
                    "low" -> LOW
                    "med", "medium" -> MEDIUM
                    "high" -> HIGH
                    else -> MEDIUM
                }
            }
        }

        override fun toString(): String {
            return when (this) {
                LOW -> "low"
                MEDIUM -> "medium"
                HIGH -> "high"
            }
        }
    }
}
