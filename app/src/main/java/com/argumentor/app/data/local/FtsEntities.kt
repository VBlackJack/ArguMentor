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

/**
 * Full-Text Search (FTS) entity for Sources.
 * Enables fast text search across source title and citation.
 *
 * Note: Unlike other FTS entities, SourceFts indexes TWO fields (title AND citation)
 * because both contain searchable content. When users search for sources, they often
 * search for author names, publication titles (in title field) or specific quotes
 * (in citation field). This design improves search relevance for sources.
 */
@Fts4(contentEntity = com.argumentor.app.data.model.Source::class)
@Entity(tableName = "sources_fts")
data class SourceFts(
    val title: String,
    val citation: String?
)

/**
 * Full-Text Search (FTS) entity for Topics.
 * Enables fast text search across topic title and summary.
 */
@Fts4(contentEntity = com.argumentor.app.data.model.Topic::class)
@Entity(tableName = "topics_fts")
data class TopicFts(
    val title: String,
    val summary: String
)

/**
 * Full-Text Search (FTS) entity for Evidences.
 * Enables fast text search across evidence content.
 */
@Fts4(contentEntity = com.argumentor.app.data.model.Evidence::class)
@Entity(tableName = "evidences_fts")
data class EvidenceFts(
    val content: String
)

/**
 * Full-Text Search (FTS) entity for Tags.
 * Enables fast text search across tag labels.
 */
@Fts4(contentEntity = com.argumentor.app.data.model.Tag::class)
@Entity(tableName = "tags_fts")
data class TagFts(
    val label: String
)
