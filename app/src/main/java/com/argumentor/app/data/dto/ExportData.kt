package com.argumentor.app.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Complete export data structure matching JSON schema v1.0.
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
