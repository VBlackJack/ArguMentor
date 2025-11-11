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
import java.time.Instant
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
            outputStream.use { stream ->
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
                stream.write(json.toByteArray())
                stream.flush()
            }

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
            // Ensure parent directory exists
            file.parentFile?.let { parent ->
                if (!parent.exists() && !parent.mkdirs()) {
                    return Result.failure(java.io.IOException("Cannot create directory: ${parent.absolutePath}"))
                }
            }

            file.outputStream().use { outputStream ->
                exportToJson(outputStream).getOrThrow()
            }
            Result.success(Unit)
        } catch (e: java.io.FileNotFoundException) {
            Result.failure(Exception("Cannot write to file: ${file.absolutePath}. ${e.message}", e))
        } catch (e: SecurityException) {
            Result.failure(Exception("Permission denied: ${file.absolutePath}. Check app permissions.", e))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("I/O error during export: ${e.message}. Check available storage space.", e))
        } catch (e: Exception) {
            Result.failure(Exception("Export failed: ${e.message}", e))
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
     * Processes import with comprehensive anti-duplicate logic.
     *
     * Import order (respects foreign key dependencies):
     * 1. Tags (no dependencies)
     * 2. Sources (no dependencies)
     * 3. Topics (references Tags)
     * 4. Claims (references Topics, uses fingerprint + similarity matching)
     * 5. Rebuttals (references Claims, uses similarity matching)
     * 6. Evidences (references Claims and Sources, validates foreign keys)
     * 7. Questions (references Topics or Claims, validates foreign keys)
     *
     * Duplicate detection strategies:
     * - **Exact ID match**: Updates if incoming is newer, otherwise marks as duplicate
     * - **Fingerprint match** (Claims): Detects identical content via normalized hash
     * - **Similarity match** (Claims, Rebuttals, Sources): Uses fuzzy text matching with configurable threshold (default 0.90)
     * - **Foreign key validation** (Evidences, Rebuttals, Questions): Ensures referenced entities exist
     *
     * @param importData The data to import
     * @param similarityThreshold Threshold for fuzzy text matching (0.0-1.0), default 0.90
     * @return ImportResult containing statistics and items flagged for review
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

        // Import Sources with fingerprint checking
        val allExistingSources = database.sourceDao().getAllSourcesSync()

        importData.sources.forEach { sourceDto ->
            try {
                val source = sourceDto.toModel()
                val fingerprint = FingerprintUtils.generateSourceFingerprint(source)

                val existing = database.sourceDao().getSourceById(source.id)
                if (existing != null) {
                    // Update if incoming is newer
                    if (isNewerTimestamp(source.updatedAt, existing.updatedAt)) {
                        database.sourceDao().updateSource(source)
                        updated++
                    } else {
                        duplicates++
                    }
                } else {
                    // Check for near-duplicates using title similarity
                    var isNearDuplicate = false
                    var similarTo: String? = null

                    for (candidate in allExistingSources) {
                        if (FingerprintUtils.areSimilar(source.title, candidate.title, similarityThreshold)) {
                            isNearDuplicate = true
                            similarTo = candidate.id
                            break
                        }
                    }

                    if (isNearDuplicate) {
                        nearDuplicates++
                        itemsForReview.add(
                            ReviewItem(
                                type = "Source",
                                incomingId = source.id,
                                existingId = similarTo ?: "",
                                incomingText = source.title.take(100),
                                existingText = allExistingSources.find { it.id == similarTo }?.title?.take(100) ?: "",
                                similarityScore = allExistingSources.find { it.id == similarTo }?.let {
                                    FingerprintUtils.similarityRatio(
                                        FingerprintUtils.normalizeText(source.title),
                                        FingerprintUtils.normalizeText(it.title)
                                    )
                                } ?: 0.0,
                                action = "review"
                            )
                        )
                    } else {
                        database.sourceDao().insertSource(source)
                        created++
                    }
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
                    // Update if incoming is newer (or if incoming has no timestamp, skip update)
                    val incomingUpdatedAt = topicDto.updatedAt
                    if (incomingUpdatedAt != null && isNewerTimestamp(incomingUpdatedAt, existing.updatedAt)) {
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

        // Import Claims with fingerprint and similarity checking
        // Use optimized batch approach to prevent OOM
        val BATCH_LIMIT = 100

        importData.claims.forEach { claimDto ->
            try {
                val claim = claimDto.toModel()
                val fingerprint = claim.claimFingerprint.ifEmpty {
                    FingerprintUtils.generateClaimFingerprint(claim)
                }

                val existing = database.claimDao().getClaimById(claim.id)
                if (existing != null) {
                    // Update if incoming is newer
                    if (isNewerTimestamp(claim.updatedAt, existing.updatedAt)) {
                        database.claimDao().updateClaim(claim.copy(claimFingerprint = fingerprint))
                        updated++
                    } else {
                        duplicates++
                    }
                } else {
                    // Check for exact fingerprint match (indexed lookup - O(1))
                    val duplicateByFingerprint = database.claimDao().getClaimByFingerprint(fingerprint)
                    if (duplicateByFingerprint != null) {
                        duplicates++
                    } else {
                        // Check for near-duplicates using similarity
                        // Load all claims and filter by topics in memory (LIKE query not precise enough)
                        val allClaims = database.claimDao().getAllClaimsSync()
                        val candidateClaims = if (claim.topics.isNotEmpty()) {
                            allClaims.filter { existingClaim ->
                                claim.topics.any { topicId -> existingClaim.topics.contains(topicId) }
                            }.take(BATCH_LIMIT)
                        } else {
                            emptyList()
                        }

                        var isNearDuplicate = false
                        var similarTo: String? = null

                        for (candidate in candidateClaims) {
                            // areSimilar now handles IllegalArgumentException internally
                            if (FingerprintUtils.areSimilar(claim.text, candidate.text, similarityThreshold)) {
                                isNearDuplicate = true
                                similarTo = candidate.id
                                break
                            }
                        }

                        if (isNearDuplicate) {
                            nearDuplicates++
                            itemsForReview.add(
                                ReviewItem(
                                    type = "Claim",
                                    incomingId = claim.id,
                                    existingId = similarTo ?: "",
                                    incomingText = claim.text.take(100),
                                    existingText = candidateClaims.find { it.id == similarTo }?.text?.take(100) ?: "",
                                    similarityScore = candidateClaims.find { it.id == similarTo }?.let {
                                        try {
                                            FingerprintUtils.similarityRatio(
                                                FingerprintUtils.normalizeText(claim.text),
                                                FingerprintUtils.normalizeText(it.text)
                                            )
                                        } catch (e: IllegalArgumentException) {
                                            0.0
                                        }
                                    } ?: 0.0,
                                    action = "review"
                                )
                            )
                        } else {
                            database.claimDao().insertClaim(claim.copy(claimFingerprint = fingerprint))
                            created++
                        }
                    }
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Claim ${claimDto.id}: ${e.message}")
            }
        }

        // Import Rebuttals with similarity checking
        val allExistingRebuttals = database.rebuttalDao().getAllRebuttalsSync()

        importData.rebuttals.forEach { rebuttalDto ->
            try {
                val rebuttal = rebuttalDto.toModel()

                // Validate foreign key: claimId must exist
                val referencedClaim = database.claimDao().getClaimById(rebuttal.claimId)
                if (referencedClaim == null) {
                    errors++
                    errorMessages.add("Rebuttal '${rebuttal.id}': Referenced claim '${rebuttal.claimId}' not found. Import the claim first or verify the claimId.")
                    return@forEach
                }

                val existing = database.rebuttalDao().getRebuttalById(rebuttal.id)
                if (existing != null) {
                    if (isNewerTimestamp(rebuttal.updatedAt, existing.updatedAt)) {
                        database.rebuttalDao().updateRebuttal(rebuttal)
                        updated++
                    } else {
                        duplicates++
                    }
                } else {
                    // Check for near-duplicates among rebuttals for the same claim
                    val candidateRebuttals = allExistingRebuttals.filter { it.claimId == rebuttal.claimId }

                    var isNearDuplicate = false
                    var similarTo: String? = null

                    for (candidate in candidateRebuttals) {
                        if (FingerprintUtils.areSimilar(rebuttal.text, candidate.text, similarityThreshold)) {
                            isNearDuplicate = true
                            similarTo = candidate.id
                            break
                        }
                    }

                    if (isNearDuplicate) {
                        nearDuplicates++
                        itemsForReview.add(
                            ReviewItem(
                                type = "Rebuttal",
                                incomingId = rebuttal.id,
                                existingId = similarTo ?: "",
                                incomingText = rebuttal.text.take(100),
                                existingText = candidateRebuttals.find { it.id == similarTo }?.text?.take(100) ?: "",
                                similarityScore = candidateRebuttals.find { it.id == similarTo }?.let {
                                    FingerprintUtils.similarityRatio(
                                        FingerprintUtils.normalizeText(rebuttal.text),
                                        FingerprintUtils.normalizeText(it.text)
                                    )
                                } ?: 0.0,
                                action = "review"
                            )
                        )
                    } else {
                        database.rebuttalDao().insertRebuttal(rebuttal)
                        created++
                    }
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Rebuttal ${rebuttalDto.id}: ${e.message}")
            }
        }

        // Import Evidences with similarity checking
        val allExistingEvidences = database.evidenceDao().getAllEvidenceSync()

        importData.evidences.forEach { evidenceDto ->
            try {
                val evidence = evidenceDto.toModel()

                // Validate foreign key: claimId must exist
                val referencedClaim = database.claimDao().getClaimById(evidence.claimId)
                if (referencedClaim == null) {
                    errors++
                    errorMessages.add("Evidence '${evidence.id}': Referenced claim '${evidence.claimId}' not found. Import the claim first or verify the claimId.")
                    return@forEach
                }

                // Validate foreign key: sourceId must exist if provided
                if (evidence.sourceId != null) {
                    val referencedSource = database.sourceDao().getSourceById(evidence.sourceId)
                    if (referencedSource == null) {
                        errors++
                        errorMessages.add("Evidence '${evidence.id}': Referenced source '${evidence.sourceId}' not found. Import the source first or remove the sourceId.")
                        return@forEach
                    }
                }

                val existing = database.evidenceDao().getEvidenceById(evidence.id)
                if (existing != null) {
                    // Update if incoming is newer
                    if (isNewerTimestamp(evidence.updatedAt, existing.updatedAt)) {
                        database.evidenceDao().updateEvidence(evidence)
                        updated++
                    } else {
                        duplicates++
                    }
                } else {
                    // Check for near-duplicates among evidences for the same claim
                    val candidateEvidences = allExistingEvidences.filter { it.claimId == evidence.claimId }

                    var isNearDuplicate = false
                    var similarTo: String? = null

                    for (candidate in candidateEvidences) {
                        if (FingerprintUtils.areSimilar(evidence.content, candidate.content, similarityThreshold)) {
                            isNearDuplicate = true
                            similarTo = candidate.id
                            break
                        }
                    }

                    if (isNearDuplicate) {
                        nearDuplicates++
                        itemsForReview.add(
                            ReviewItem(
                                type = "Evidence",
                                incomingId = evidence.id,
                                existingId = similarTo ?: "",
                                incomingText = evidence.content.take(100),
                                existingText = candidateEvidences.find { it.id == similarTo }?.content?.take(100) ?: "",
                                similarityScore = candidateEvidences.find { it.id == similarTo }?.let {
                                    FingerprintUtils.similarityRatio(
                                        FingerprintUtils.normalizeText(evidence.content),
                                        FingerprintUtils.normalizeText(it.content)
                                    )
                                } ?: 0.0,
                                action = "review"
                            )
                        )
                    } else {
                        database.evidenceDao().insertEvidence(evidence)
                        created++
                    }
                }
            } catch (e: Exception) {
                errors++
                errorMessages.add("Evidence ${evidenceDto.id}: ${e.message}")
            }
        }

        // Import Questions
        importData.questions.forEach { questionDto ->
            try {
                val question = questionDto.toModel()

                // Validate foreign key: targetId must exist (as Topic or Claim)
                val targetAsTopic = database.topicDao().getTopicById(question.targetId)
                val targetAsClaim = database.claimDao().getClaimById(question.targetId)
                if (targetAsTopic == null && targetAsClaim == null) {
                    errors++
                    errorMessages.add("Question '${question.id}': Referenced target '${question.targetId}' not found. The targetId must reference an existing Topic or Claim. Import the target first or verify the targetId.")
                    return@forEach
                }

                val existing = database.questionDao().getQuestionById(question.id)
                if (existing != null) {
                    duplicates++
                } else {
                    database.questionDao().insertQuestion(question)
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

    // Utility functions for timestamp validation and comparison

    /**
     * Validates if a string is a valid ISO 8601 timestamp.
     * @return true if valid, false otherwise
     */
    private fun isValidIsoTimestamp(timestamp: String): Boolean {
        return try {
            Instant.parse(timestamp)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Compares two ISO 8601 timestamps.
     * @return true if first is newer than second, false otherwise
     */
    private fun isNewerTimestamp(first: String, second: String): Boolean {
        return try {
            val firstInstant = Instant.parse(first)
            val secondInstant = Instant.parse(second)
            firstInstant.isAfter(secondInstant)
        } catch (e: Exception) {
            // Fallback to string comparison if parsing fails
            first.compareTo(second) > 0
        }
    }

    // Helper functions to get all data
    private suspend fun getAllTopics(): List<TopicDto> {
        return database.topicDao().getAllTopicsSync().map { it.toDto() }
    }

    private suspend fun getAllClaims(): List<ClaimDto> {
        return database.claimDao().getAllClaimsSync().map { it.toDto() }
    }

    private suspend fun getAllRebuttals(): List<RebuttalDto> {
        return database.rebuttalDao().getAllRebuttalsSync().map { it.toDto() }
    }

    private suspend fun getAllEvidences(): List<EvidenceDto> {
        return database.evidenceDao().getAllEvidenceSync().map { it.toDto() }
    }

    private suspend fun getAllQuestions(): List<QuestionDto> {
        return database.questionDao().getAllQuestionsSync().map { it.toDto() }
    }

    private suspend fun getAllSources(): List<SourceDto> {
        return database.sourceDao().getAllSourcesSync().map { it.toDto() }
    }

    private suspend fun getAllTags(): List<TagDto> {
        return database.tagDao().getAllTagsSync().map { it.toDto() }
    }
}
