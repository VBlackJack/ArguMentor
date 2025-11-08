package com.argumentor.app.data.local

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Full-Text Search (FTS) entity for Claims.
 * Enables fast text search across claim content.
 */
@Fts4(contentEntity = com.argumentor.app.data.model.Claim::class)
@Entity(tableName = "claims_fts")
data class ClaimFts(
    val text: String
)

/**
 * Full-Text Search (FTS) entity for Rebuttals.
 * Enables fast text search across rebuttal content.
 */
@Fts4(contentEntity = com.argumentor.app.data.model.Rebuttal::class)
@Entity(tableName = "rebuttals_fts")
data class RebuttalFts(
    val text: String
)

/**
 * Full-Text Search (FTS) entity for Questions.
 * Enables fast text search across question content.
 */
@Fts4(contentEntity = com.argumentor.app.data.model.Question::class)
@Entity(tableName = "questions_fts")
data class QuestionFts(
    val text: String
)
