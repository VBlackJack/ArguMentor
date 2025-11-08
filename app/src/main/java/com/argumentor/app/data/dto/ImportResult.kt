package com.argumentor.app.data.dto

/**
 * Result of an import operation with detailed statistics.
 */
data class ImportResult(
    val success: Boolean,
    val totalItems: Int,
    val created: Int,
    val updated: Int,
    val duplicates: Int,
    val nearDuplicates: Int,
    val errors: Int,
    val errorMessages: List<String> = emptyList(),
    val itemsForReview: List<ReviewItem> = emptyList()
)

/**
 * Item that needs manual review (near-duplicate or conflict).
 */
data class ReviewItem(
    val type: String, // "claim", "rebuttal", "source", "topic"
    val newItem: Any,
    val existingItem: Any,
    val reason: String, // "near_duplicate", "conflict"
    val similarityScore: Double? = null
)
