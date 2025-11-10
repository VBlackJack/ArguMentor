package com.argumentor.app.data.model

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
 */
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
