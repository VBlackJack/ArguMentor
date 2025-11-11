package com.argumentor.app.data.dto

import com.argumentor.app.data.model.*
import com.argumentor.app.util.FingerprintUtils

/**
 * Extension functions to convert between domain models and DTOs.
 */

// Topic mappers
fun Topic.toDto(): TopicDto = TopicDto(
    id = id,
    title = title,
    summary = summary,
    posture = posture.toString(),
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TopicDto.toModel(): Topic = Topic(
    id = id,
    title = title,
    summary = summary,
    posture = Topic.Posture.fromString(posture),
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Claim mappers
fun Claim.toDto(): ClaimDto = ClaimDto(
    id = id,
    text = text,
    stance = stance.toString(),
    strength = strength.toString(),
    topics = topics,
    createdAt = createdAt,
    updatedAt = updatedAt,
    claimFingerprint = claimFingerprint.ifEmpty { FingerprintUtils.generateTextFingerprint(text) }
)

fun ClaimDto.toModel(): Claim = Claim(
    id = id,
    text = text,
    stance = Claim.Stance.fromString(stance),
    strength = Claim.Strength.fromString(strength),
    topics = topics,
    createdAt = createdAt,
    updatedAt = updatedAt,
    claimFingerprint = claimFingerprint ?: FingerprintUtils.generateTextFingerprint(text)
)

// Rebuttal mappers
fun Rebuttal.toDto(): RebuttalDto = RebuttalDto(
    id = id,
    claimId = claimId,
    text = text,
    fallacyTag = fallacyTag,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RebuttalDto.toModel(): Rebuttal = Rebuttal(
    id = id,
    claimId = claimId,
    text = text,
    fallacyTag = fallacyTag,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Evidence mappers
fun Evidence.toDto(): EvidenceDto = EvidenceDto(
    id = id,
    claimId = claimId,
    type = type.toString(),
    content = content,
    sourceId = sourceId,
    quality = quality.toString(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun EvidenceDto.toModel(): Evidence = Evidence(
    id = id,
    claimId = claimId,
    type = Evidence.EvidenceType.fromString(type),
    content = content,
    sourceId = sourceId,
    quality = Evidence.Quality.fromString(quality),
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Question mappers
fun Question.toDto(): QuestionDto = QuestionDto(
    id = id,
    targetId = targetId,
    text = text,
    kind = kind.toString(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun QuestionDto.toModel(): Question = Question(
    id = id,
    targetId = targetId,
    text = text,
    kind = Question.QuestionKind.fromString(kind),
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Source mappers
fun Source.toDto(): SourceDto = SourceDto(
    id = id,
    title = title,
    citation = citation,
    url = url,
    publisher = publisher,
    date = date,
    reliabilityScore = reliabilityScore,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SourceDto.toModel(): Source {
    // Validate reliabilityScore if present
    reliabilityScore?.let { score ->
        require(score in 0.0..1.0) {
            "Source '${id}': reliabilityScore must be between 0.0 and 1.0, got $score"
        }
    }
    return Source(
        id = id,
        title = title,
        citation = citation,
        url = url,
        publisher = publisher,
        date = date,
        reliabilityScore = reliabilityScore,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Tag mappers
fun Tag.toDto(): TagDto = TagDto(
    id = id,
    label = label,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TagDto.toModel(): Tag = Tag(
    id = id,
    label = label,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt
)
