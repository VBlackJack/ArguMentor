package com.argumentor.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Rebuttal entity representing a counter-argument to a claim.
 *
 * @property id Unique identifier (UUID v4)
 * @property claimId Reference to the parent claim
 * @property text The rebuttal text content
 * @property fallacyTag Optional fallacy tag (ad_hominem, straw_man, etc.)
 * @property createdAt Creation timestamp in ISO 8601 format
 * @property updatedAt Last update timestamp in ISO 8601 format
 *
 * Note: When this rebuttal is deleted, the parent claim (if exists) remains unchanged.
 * When the parent claim is deleted, this rebuttal is automatically deleted (CASCADE).
 */
@Entity(
    tableName = "rebuttals",
    foreignKeys = [
        ForeignKey(
            entity = Claim::class,
            parentColumns = ["id"],
            childColumns = ["claimId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("claimId")]
)
data class Rebuttal(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val claimId: String,
    val text: String,
    val fallacyTag: String? = null,
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
)
