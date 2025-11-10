package com.argumentor.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Tag entity for categorizing topics and claims.
 *
 * @property id Unique identifier (UUID v4)
 * @property label Tag label (e.g., "religion", "histoire", "science")
 * @property color Optional color code for UI display
 * @property createdAt ISO 8601 timestamp of creation
 * @property updatedAt ISO 8601 timestamp of last update
 */
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val color: String? = null,
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
)
