package com.argumentor.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Evidence entity representing proof/examples supporting a claim.
 *
 * @property id Unique identifier (UUID v4)
 * @property claimId Reference to the parent claim
 * @property type Evidence type: study, stat, quote, example
 * @property content The evidence content
 * @property sourceId Optional reference to a Source entity
 * @property quality Quality rating: low, medium, high
 * @property createdAt Creation timestamp in ISO 8601 format
 * @property updatedAt Last update timestamp in ISO 8601 format
 *
 * Note: When this evidence is deleted, the parent claim (if exists) remains unchanged.
 * When the parent claim is deleted, this evidence is automatically deleted (CASCADE).
 * When the referenced source is deleted, sourceId is set to NULL (SET NULL).
 */
@Entity(
    tableName = "evidences",
    foreignKeys = [
        ForeignKey(
            entity = Claim::class,
            parentColumns = ["id"],
            childColumns = ["claimId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Source::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("claimId"), Index("sourceId")]
)
data class Evidence(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val claimId: String,
    val type: EvidenceType,
    val content: String,
    val sourceId: String? = null,
    val quality: Quality = Quality.MEDIUM,
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
) {
    enum class EvidenceType {
        STUDY, STAT, QUOTE, EXAMPLE;

        companion object {
            fun fromString(value: String): EvidenceType {
                return when (value.lowercase()) {
                    "study" -> STUDY
                    "stat" -> STAT
                    "quote" -> QUOTE
                    "example" -> EXAMPLE
                    else -> EXAMPLE
                }
            }
        }

        override fun toString(): String {
            return name.lowercase()
        }
    }

    enum class Quality {
        LOW, MEDIUM, HIGH;

        companion object {
            fun fromString(value: String): Quality {
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
