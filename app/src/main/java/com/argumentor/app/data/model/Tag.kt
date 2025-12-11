package com.argumentor.app.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
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
 *
 * QUALITY-001: @Immutable annotation helps Compose skip recomposition when data hasn't changed.
 * This data class is immutable (all properties are val) and contains only immutable types.
 *
 * PERF-001: Index on label for faster lookup in TagDao.getTagByLabel()
 */
@Immutable
@Entity(
    tableName = "tags",
    indices = [Index(value = ["label"])]
)
data class Tag(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val color: String? = null,
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
)
