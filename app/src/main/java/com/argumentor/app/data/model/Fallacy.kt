package com.argumentor.app.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Fallacy entity representing a logical fallacy/sophism.
 *
 * This entity allows for CRUD operations on fallacies, making them editable
 * instead of being hardcoded in the application.
 *
 * @property id Unique identifier (UUID v4 or predefined ID like "ad_hominem")
 * @property name The fallacy name (e.g., "Ad Hominem", "Texas Sharpshooter")
 * @property description Detailed explanation of the fallacy
 * @property example An illustrative example of the fallacy in use
 * @property category Optional category for grouping (e.g., "Emotional Appeal", "Logical Structure")
 * @property isCustom Whether this is a user-created fallacy (vs. pre-loaded from catalog)
 * @property createdAt Creation timestamp in ISO 8601 format
 * @property updatedAt Last update timestamp in ISO 8601 format
 *
 * QUALITY-001: @Immutable annotation helps Compose skip recomposition when data hasn't changed.
 */
@Immutable
@Entity(tableName = "fallacies")
data class Fallacy(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val example: String,
    val category: String = "",
    val isCustom: Boolean = false,
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
)
