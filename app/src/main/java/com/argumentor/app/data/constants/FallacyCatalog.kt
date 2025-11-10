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
        "bandwagon"
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
     * Helper function to get a string resource by name.
     */
    private fun getStringResource(context: Context, resourceName: String): String {
        val resourceId = context.resources.getIdentifier(
            resourceName,
            "string",
            context.packageName
        )
        return if (resourceId != 0) {
            context.getString(resourceId)
        } else {
            resourceName // Fallback to resource name if not found
        }
    }
}
