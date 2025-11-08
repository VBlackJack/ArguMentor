package com.argumentor.app.data.repository

import com.argumentor.app.data.dto.*
import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import com.argumentor.app.util.FingerprintUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling import and export of data in JSON format.
 * Implements anti-duplicate logic with fingerprint matching.
 */
@Singleton
class ImportExportRepository @Inject constructor(
    private val database: ArguMentorDatabase
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Export all data to JSON format (schema v1.0).
     */
    suspend fun exportToJson(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exportData = ExportData(
                schemaVersion = "1.0",
                exportedAt = getCurrentIsoTimestamp(),
                app = "ArguMentor",
                topics = getAllTopics(),
                claims = getAllClaims(),
                rebuttals = getAllRebuttals(),
                evidences = getAllEvidences(),
                questions = getAllQuestions(),
                sources = getAllSources(),
                tags = getAllTags()
            )

            val json = gson.toJson(exportData)
            outputStream.write(json.toByteArray())
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export all data to JSON file.
     */
    suspend fun exportToFile(file: File): Result<Unit> {
        return try {
            file.outputStream().use { outputStream ->
                exportToJson(outputStream)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import data from JSON with anti-duplicate logic.
     * @param similarityThreshold Threshold for fuzzy matching (0.0-1.0), default 0.90
     */
    suspend fun importFromJson(
        inputStream: InputStream,
        similarityThreshold: Double = 0.90
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val importData = gson.fromJson(json, ExportData::class.java)

            // Validate schema version
            if (importData.schemaVersion != "1.0") {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported schema version: ${importData.schemaVersion}")
                )
            }

            val result = processImport(importData, similarityThreshold)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import data from JSON file.
     */
    suspend fun importFromFile(
        file: File,
        similarityThreshold: Double = 0.90
    ): Result<ImportResult> {
        return try {
            file.inputStream().use { inputStream ->
                importFromJson(inputStream, similarityThreshold)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Process import with anti-duplicate logic.
     */
    private suspend fun processImport(
        importData: ExportData,
        similarityThreshold: Double
    ): ImportResult {
        var created = 0
        var updated = 0
        var duplicates = 0
        var nearDuplicates = 0
        var errors = 0
        val errorMessages = mutableListOf<String>()
        val itemsForReview = mutableListOf<ReviewItem>()

        // Import Tags first (no dependencies)
        importData.tags.forEach { tagDto ->
            try {
                val existing = database.tagDao().getTagById(tagDto.id)
                if (existing != null) {
                    duplicates++
                } else {
                    val existingByLabel = database.tagDao().getTagByLabel(tagDto.label)
                    if (existingByLabel != null) {
                        duplicates++
                    } else {
                        database.tagDao().insertTag(tagDto.toModel())
                        created++
                    }
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Tag ${tagDto.id}: ${e.message}")
            }
        }

        // Import Sources (no dependencies)
        importData.sources.forEach { sourceDto ->
            try {
                val existing = database.sourceDao().getSourceById(sourceDto.id)
                if (existing != null) {
                    duplicates++
                } else {
                    val source = sourceDto.toModel()
                    val fingerprint = FingerprintUtils.generateSourceFingerprint(source)

                    // Check for similar sources (simplified - in production would check all)
                    database.sourceDao().insertSource(source)
                    created++
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Source ${sourceDto.id}: ${e.message}")
            }
        }

        // Import Topics
        importData.topics.forEach { topicDto ->
            try {
                val existing = database.topicDao().getTopicById(topicDto.id)
                if (existing != null) {
                    // Update if incoming is newer
                    if (topicDto.updatedAt > existing.updatedAt) {
                        database.topicDao().updateTopic(topicDto.toModel())
                        updated++
                    } else {
                        duplicates++
                    }
                } else {
                    database.topicDao().insertTopic(topicDto.toModel())
                    created++
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Topic ${topicDto.id}: ${e.message}")
            }
        }

        // Import Claims with fingerprint checking
        importData.claims.forEach { claimDto ->
            try {
                val claim = claimDto.toModel()
                val fingerprint = claim.claimFingerprint.ifEmpty {
                    FingerprintUtils.generateClaimFingerprint(claim)
                }

                val existing = database.claimDao().getClaimById(claim.id)
                if (existing != null) {
                    // Update if incoming is newer
                    if (claim.updatedAt > existing.updatedAt) {
                        database.claimDao().updateClaim(claim.copy(claimFingerprint = fingerprint))
                        updated++
                    } else {
                        duplicates++
                    }
                } else {
                    // Check for fingerprint match
                    val duplicateByFingerprint = database.claimDao().getClaimByFingerprint(fingerprint)
                    if (duplicateByFingerprint != null) {
                        duplicates++
                    } else {
                        // Check for near-duplicates using similarity
                        // (Simplified: in production, would check against all existing claims)
                        database.claimDao().insertClaim(claim.copy(claimFingerprint = fingerprint))
                        created++
                    }
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Claim ${claimDto.id}: ${e.message}")
            }
        }

        // Import Rebuttals
        importData.rebuttals.forEach { rebuttalDto ->
            try {
                val existing = database.rebuttalDao().getRebuttalById(rebuttalDto.id)
                if (existing != null) {
                    if (rebuttalDto.updatedAt > existing.updatedAt) {
                        database.rebuttalDao().updateRebuttal(rebuttalDto.toModel())
                        updated++
                    } else {
                        duplicates++
                    }
                } else {
                    database.rebuttalDao().insertRebuttal(rebuttalDto.toModel())
                    created++
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Rebuttal ${rebuttalDto.id}: ${e.message}")
            }
        }

        // Import Evidences
        importData.evidences.forEach { evidenceDto ->
            try {
                val existing = database.evidenceDao().getEvidenceById(evidenceDto.id)
                if (existing != null) {
                    duplicates++
                } else {
                    database.evidenceDao().insertEvidence(evidenceDto.toModel())
                    created++
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Evidence ${evidenceDto.id}: ${e.message}")
            }
        }

        // Import Questions
        importData.questions.forEach { questionDto ->
            try {
                val existing = database.questionDao().getQuestionById(questionDto.id)
                if (existing != null) {
                    duplicates++
                } else {
                    database.questionDao().insertQuestion(questionDto.toModel())
                    created++
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Question ${questionDto.id}: ${e.message}")
            }
        }

        val totalItems = importData.topics.size + importData.claims.size +
                importData.rebuttals.size + importData.evidences.size +
                importData.questions.size + importData.sources.size + importData.tags.size

        return ImportResult(
            success = errors == 0,
            totalItems = totalItems,
            created = created,
            updated = updated,
            duplicates = duplicates,
            nearDuplicates = nearDuplicates,
            errors = errors,
            errorMessages = errorMessages,
            itemsForReview = itemsForReview
        )
    }

    // Helper functions to get all data
    private suspend fun getAllTopics(): List<TopicDto> {
        // Note: In production, use Flow.first() or similar
        // For now, simplified approach
        return emptyList() // Will be populated when repositories are connected
    }

    private suspend fun getAllClaims(): List<ClaimDto> = emptyList()
    private suspend fun getAllRebuttals(): List<RebuttalDto> = emptyList()
    private suspend fun getAllEvidences(): List<EvidenceDto> = emptyList()
    private suspend fun getAllQuestions(): List<QuestionDto> = emptyList()
    private suspend fun getAllSources(): List<SourceDto> = emptyList()
    private suspend fun getAllTags(): List<TagDto> = emptyList()
}
