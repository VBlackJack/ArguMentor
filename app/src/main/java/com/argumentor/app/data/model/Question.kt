package com.argumentor.app.data.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Question entity representing questions about topics or claims.
 *
 * @property id Unique identifier (UUID v4)
 * @property targetId Reference to topic or claim ID
 * @property text The question text
 * @property kind Question type: socratic or clarifying
 * @property createdAt Creation timestamp in ISO 8601 format
 * @property updatedAt Last update timestamp in ISO 8601 format
 *
 * Note on Foreign Keys:
 * This entity intentionally does NOT define a Foreign Key on `targetId` because:
 * 1. targetId is POLYMORPHIC - it can reference either a Topic OR a Claim
 * 2. Room/SQLite does not support polymorphic Foreign Keys
 * 3. Orphan questions are cleaned up manually via QuestionDao.deleteOrphanQuestions()
 *
 * Alternative considered: Creating separate tables (TopicQuestion, ClaimQuestion)
 * with proper FKs, but this would significantly complicate queries and the domain model.
 * Current approach provides better flexibility for question management.
 *
 * QUALITY-001: @Immutable annotation helps Compose skip recomposition when data hasn't changed.
 * This data class is immutable (all properties are val) and contains only immutable types.
 */
@Immutable
@Entity(
    tableName = "questions",
    indices = [Index("targetId")]
)
data class Question(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val targetId: String,
    val text: String,
    val kind: QuestionKind = QuestionKind.CLARIFYING,
    val createdAt: String = getCurrentIsoTimestamp(),
    val updatedAt: String = getCurrentIsoTimestamp()
) {
    enum class QuestionKind {
        SOCRATIC, CLARIFYING, CHALLENGE, EVIDENCE;

        companion object {
            fun fromString(value: String): QuestionKind {
                return when (value.lowercase()) {
                    "socratic" -> SOCRATIC
                    "clarifying" -> CLARIFYING
                    "challenge" -> CHALLENGE
                    "evidence" -> EVIDENCE
                    else -> CLARIFYING
                }
            }
        }

        override fun toString(): String {
            return name.lowercase()
        }
    }
}
