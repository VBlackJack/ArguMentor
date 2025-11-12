package com.argumentor.app.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Complete export data structure for ArguMentor data exchange.
 *
 * LOW-002 FIX: Enhanced documentation with comprehensive schema information.
 *
 * ## JSON Schema Version
 * Current version: **1.0**
 *
 * ## Purpose
 * This structure represents the complete export format for all ArguMentor data,
 * including topics, claims, rebuttals, evidences, questions, sources, and tags.
 * The schema is versioned to support future format changes and backward compatibility.
 *
 * ## Schema Evolution
 * - **Version 1.0** (Current): Initial release with all core entities
 *   - Topics, Claims, Rebuttals, Evidences, Questions, Sources, Tags
 *   - All timestamps in ISO 8601 format
 *   - All IDs as UUID v4 strings
 *
 * ## Backward Compatibility
 * Future schema versions will maintain backward compatibility by:
 * - Adding optional fields only (never removing required fields)
 * - Providing default values for new fields
 * - Supporting migration from older schema versions
 *
 * ## Usage
 * ```kotlin
 * // Export
 * val exportData = ExportData(
 *     exportedAt = getCurrentIsoTimestamp(),
 *     topics = topicList.map { it.toDto() },
 *     claims = claimList.map { it.toDto() }
 *     // ... other entities
 * )
 * val json = Gson().toJson(exportData)
 *
 * // Import
 * val importData = Gson().fromJson(json, ExportData::class.java)
 * // Validate schema version before processing
 * ```
 *
 * @property schemaVersion The version of the export schema (currently "1.0")
 * @property exportedAt ISO 8601 timestamp of when this export was created
 * @property app Application identifier (always "ArguMentor")
 * @property topics List of all exported topics
 * @property claims List of all exported claims
 * @property rebuttals List of all exported rebuttals
 * @property evidences List of all exported evidences
 * @property questions List of all exported questions
 * @property sources List of all exported sources (bibliographic references)
 * @property tags List of all exported tags (custom categorization)
 *
 * @see TopicDto
 * @see ClaimDto
 * @see RebuttalDto
 * @see EvidenceDto
 * @see QuestionDto
 * @see SourceDto
 * @see TagDto
 */
data class ExportData(
    @SerializedName("schemaVersion")
    val schemaVersion: String = "1.0",

    @SerializedName("exportedAt")
    val exportedAt: String,

    @SerializedName("app")
    val app: String = "ArguMentor",

    @SerializedName("topics")
    val topics: List<TopicDto> = emptyList(),

    @SerializedName("claims")
    val claims: List<ClaimDto> = emptyList(),

    @SerializedName("rebuttals")
    val rebuttals: List<RebuttalDto> = emptyList(),

    @SerializedName("evidences")
    val evidences: List<EvidenceDto> = emptyList(),

    @SerializedName("questions")
    val questions: List<QuestionDto> = emptyList(),

    @SerializedName("sources")
    val sources: List<SourceDto> = emptyList(),

    @SerializedName("tags")
    val tags: List<TagDto> = emptyList()
)

data class TopicDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("summary")
    val summary: String,

    @SerializedName("posture")
    val posture: String,

    @SerializedName("tags")
    val tags: List<String> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class ClaimDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("stance")
    val stance: String,

    @SerializedName("strength")
    val strength: String,

    @SerializedName("topics")
    val topics: List<String>,

    @SerializedName("fallacyIds")
    val fallacyIds: List<String> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("claimFingerprint")
    val claimFingerprint: String? = null
)

data class RebuttalDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("claimId")
    val claimId: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("fallacyIds")
    val fallacyIds: List<String> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class EvidenceDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("claimId")
    val claimId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("sourceId")
    val sourceId: String? = null,

    @SerializedName("quality")
    val quality: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class QuestionDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("targetId")
    val targetId: String,

    @SerializedName("text")
    val text: String,

    @SerializedName("kind")
    val kind: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class SourceDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("citation")
    val citation: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("publisher")
    val publisher: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("reliabilityScore")
    val reliabilityScore: Double? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class TagDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("color")
    val color: String? = null,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)
