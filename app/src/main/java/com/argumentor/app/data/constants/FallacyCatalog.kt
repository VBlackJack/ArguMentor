package com.argumentor.app.data.constants

import android.content.Context
import com.argumentor.app.R

/**
 * Catalog of logical fallacies and sophisms.
 * Used for tagging rebuttals and educational purposes.
 */
object FallacyCatalog {
    data class Fallacy(
        val id: String,
        val name: String,
        val description: String,
        val example: String
    )

    /**
     * List of all fallacy IDs.
     * This is static and language-independent.
     */
    private val FALLACY_IDS = listOf(
        "ad_hominem",
        "straw_man",
        "appeal_to_ignorance",
        "post_hoc",
        "false_dilemma",
        "begging_question",
        "slippery_slope",
        "postdiction",
        "cherry_picking",
        "appeal_to_tradition",
        "appeal_to_authority",
        "appeal_to_popularity",
        "circular_reasoning",
        "tu_quoque",
        "hasty_generalization",
        "red_herring",
        "no_true_scotsman",
        "loaded_question",
        "appeal_to_emotion",
        "appeal_to_nature",
        "false_equivalence",
        "burden_of_proof",
        "texas_sharpshooter",
        "middle_ground",
        "anecdotal",
        "composition",
        "division",
        "genetic_fallacy",
        "bandwagon",
        "appeal_to_fear"
    )

    /**
     * Get the full list of fallacies in the current language.
     */
    fun getFallacies(context: Context): List<Fallacy> {
        return FALLACY_IDS.map { id ->
            Fallacy(
                id = id,
                name = getStringResource(context, "fallacy_${id}_name"),
                description = getStringResource(context, "fallacy_${id}_description"),
                example = getStringResource(context, "fallacy_${id}_example")
            )
        }
    }

    /**
     * Get a fallacy by its ID in the current language.
     */
    fun getFallacyById(context: Context, id: String): Fallacy? {
        if (!FALLACY_IDS.contains(id)) return null

        return Fallacy(
            id = id,
            name = getStringResource(context, "fallacy_${id}_name"),
            description = getStringResource(context, "fallacy_${id}_description"),
            example = getStringResource(context, "fallacy_${id}_example")
        )
    }

    /**
     * Search fallacies by name or description in the current language.
     */
    fun searchFallacies(context: Context, query: String): List<Fallacy> {
        val lowerQuery = query.lowercase()
        return getFallacies(context).filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
        }
    }

    /**
     * Cache for string resource IDs to avoid repeated reflection calls.
     * MEDIUM-004 FIX: getIdentifier() uses reflection which is 10-100x slower than direct access.
     * This cache reduces lookup time from O(n) reflection to O(1) HashMap access.
     *
     * The cache is cleared automatically when the app process is killed (no manual cleanup needed).
     * For dynamic language changes within the same process, consider clearing this cache.
     */
    private val resourceIdCache = mutableMapOf<String, Int>()

    /**
     * Helper function to get a string resource by name with caching.
     *
     * MEDIUM-004 FIX: Added resource ID caching to avoid repeated expensive reflection calls.
     * Performance improvement: ~10-100x faster for repeated lookups.
     *
     * @param context Context to access resources
     * @param resourceName Name of the string resource (e.g., "fallacy_ad_hominem_name")
     * @return The localized string, or the resource name as fallback if not found
     */
    private fun getStringResource(context: Context, resourceName: String): String {
        // Check cache first (O(1) HashMap lookup)
        val cachedResourceId = resourceIdCache[resourceName]
        val resourceId = if (cachedResourceId != null) {
            cachedResourceId
        } else {
            // Cache miss - use reflection to find resource ID (expensive O(n) operation)
            val id = context.resources.getIdentifier(
                resourceName,
                "string",
                context.packageName
            )
            // Cache the result for future lookups
            resourceIdCache[resourceName] = id
            id
        }

        return if (resourceId != 0) {
            context.getString(resourceId)
        } else {
            resourceName // Fallback to resource name if not found
        }
    }
}
