package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.FallacyDao
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing fallacy entities.
 * Handles CRUD operations and search functionality for logical fallacies.
 */
@Singleton
class FallacyRepository @Inject constructor(
    private val fallacyDao: FallacyDao
) {
    /**
     * Observes all fallacies in the database.
     * @return Flow emitting list of all fallacies, updated automatically when data changes
     */
    fun getAllFallacies(): Flow<List<Fallacy>> = fallacyDao.getAllFallacies()

    /**
     * Retrieves all fallacies synchronously (one-time fetch).
     * @return List of all fallacies
     */
    suspend fun getAllFallaciesSync(): List<Fallacy> = fallacyDao.getAllFallaciesSync()

    /**
     * Observes a specific fallacy by ID.
     * @param fallacyId The unique identifier of the fallacy
     * @return Flow emitting the fallacy or null if not found
     */
    fun getFallacyById(fallacyId: String): Flow<Fallacy?> = fallacyDao.observeFallacyById(fallacyId)

    /**
     * Retrieves a fallacy by ID (one-time fetch).
     * @param fallacyId The unique identifier of the fallacy
     * @return The fallacy or null if not found
     */
    suspend fun getFallacyByIdSync(fallacyId: String): Fallacy? = fallacyDao.getFallacyById(fallacyId)

    /**
     * Retrieves multiple fallacies by their IDs in a single query.
     * PERF-001 FIX: Uses bulk query instead of N+1 pattern.
     * @param fallacyIds List of fallacy identifiers
     * @return List of fallacies (may contain fewer items if some IDs don't exist)
     */
    suspend fun getFallaciesByIds(fallacyIds: List<String>): List<Fallacy> {
        return if (fallacyIds.isEmpty()) {
            emptyList()
        } else {
            fallacyDao.getFallaciesByIds(fallacyIds)
        }
    }

    /**
     * Searches fallacies by name or description.
     * @param query Search query
     * @return Flow emitting list of matching fallacies
     */
    fun searchFallacies(query: String): Flow<List<Fallacy>> {
        return if (query.isBlank()) {
            getAllFallacies()
        } else {
            fallacyDao.searchFallacies(query)
        }
    }

    /**
     * Gets fallacies by category.
     * @param category The category name
     * @return Flow emitting list of fallacies in the category
     */
    fun getFallaciesByCategory(category: String): Flow<List<Fallacy>> =
        fallacyDao.getFallaciesByCategory(category)

    /**
     * Gets all custom (user-created) fallacies.
     * @return Flow emitting list of custom fallacies
     */
    fun getCustomFallacies(): Flow<List<Fallacy>> = fallacyDao.getCustomFallacies()

    /**
     * Gets all pre-loaded (non-custom) fallacies.
     * @return Flow emitting list of pre-loaded fallacies
     */
    fun getPreloadedFallacies(): Flow<List<Fallacy>> = fallacyDao.getPreloadedFallacies()

    /**
     * Inserts a new fallacy.
     * @param fallacy The fallacy to insert
     */
    suspend fun insertFallacy(fallacy: Fallacy) {
        // Validation
        require(fallacy.name.isNotBlank()) { "Fallacy name cannot be blank" }
        require(fallacy.description.isNotBlank()) { "Fallacy description cannot be blank" }

        fallacyDao.insertFallacy(fallacy)
    }

    /**
     * Inserts multiple fallacies.
     * @param fallacies List of fallacies to insert
     */
    suspend fun insertFallacies(fallacies: List<Fallacy>) {
        fallacyDao.insertFallacies(fallacies)
    }

    /**
     * Updates an existing fallacy.
     * @param fallacy The fallacy to update
     */
    suspend fun updateFallacy(fallacy: Fallacy) {
        // Validation
        require(fallacy.name.isNotBlank()) { "Fallacy name cannot be blank" }
        require(fallacy.description.isNotBlank()) { "Fallacy description cannot be blank" }

        // Update the updatedAt timestamp
        val updatedFallacy = fallacy.copy(
            updatedAt = com.argumentor.app.data.model.getCurrentIsoTimestamp()
        )
        fallacyDao.updateFallacy(updatedFallacy)
    }

    /**
     * Deletes a fallacy.
     * @param fallacy The fallacy to delete
     */
    suspend fun deleteFallacy(fallacy: Fallacy) {
        fallacyDao.deleteFallacy(fallacy)
    }

    /**
     * Deletes a fallacy by ID.
     * @param fallacyId The unique identifier of the fallacy to delete
     */
    suspend fun deleteFallacyById(fallacyId: String) {
        fallacyDao.deleteFallacyById(fallacyId)
    }

    /**
     * Checks if a fallacy exists by ID.
     * @param fallacyId The unique identifier to check
     * @return True if the fallacy exists, false otherwise
     */
    suspend fun fallacyExists(fallacyId: String): Boolean = fallacyDao.fallacyExists(fallacyId)

    /**
     * Gets the total count of fallacies.
     * @return The number of fallacies in the database
     */
    suspend fun getFallacyCount(): Int = fallacyDao.getFallacyCount()

    /**
     * Gets the count of custom (user-created) fallacies.
     * @return The number of custom fallacies
     */
    suspend fun getCustomFallacyCount(): Int = fallacyDao.getCustomFallacyCount()

    /**
     * Ensures default fallacies are loaded in the database.
     * This fixes the issue where first-time installations don't have any fallacies
     * because the migration only runs on database upgrades, not on initial creation.
     *
     * BUG FIX: The MIGRATION_9_10 only runs when upgrading from version 9 to 10.
     * If a user installs the app for the first time after version 10, the migration
     * never runs and the fallacies table remains empty.
     *
     * This method should be called once at app startup to ensure the default
     * fallacies are always available.
     */
    suspend fun ensureDefaultFallaciesExist() {
        try {
            val count = getFallacyCount()

            // Only insert defaults if the database is completely empty
            if (count == 0) {
                Timber.d("No fallacies found in database. Inserting 30 default fallacies...")

                val currentTime = getCurrentIsoTimestamp()
                val defaultFallacies = listOf(
                    "ad_hominem" to "Ad Hominem",
                    "straw_man" to "Straw Man",
                    "appeal_to_ignorance" to "Appeal to Ignorance",
                    "post_hoc" to "Post Hoc",
                    "false_dilemma" to "False Dilemma",
                    "begging_question" to "Begging the Question",
                    "slippery_slope" to "Slippery Slope",
                    "postdiction" to "Postdiction",
                    "cherry_picking" to "Cherry Picking",
                    "appeal_to_tradition" to "Appeal to Tradition",
                    "appeal_to_authority" to "Appeal to Authority",
                    "appeal_to_popularity" to "Appeal to Popularity",
                    "circular_reasoning" to "Circular Reasoning",
                    "tu_quoque" to "Tu Quoque",
                    "hasty_generalization" to "Hasty Generalization",
                    "red_herring" to "Red Herring",
                    "no_true_scotsman" to "No True Scotsman",
                    "loaded_question" to "Loaded Question",
                    "appeal_to_emotion" to "Appeal to Emotion",
                    "appeal_to_nature" to "Appeal to Nature",
                    "false_equivalence" to "False Equivalence",
                    "burden_of_proof" to "Burden of Proof",
                    "texas_sharpshooter" to "Texas Sharpshooter",
                    "middle_ground" to "Middle Ground",
                    "anecdotal" to "Anecdotal",
                    "composition" to "Composition",
                    "division" to "Division",
                    "genetic_fallacy" to "Genetic Fallacy",
                    "bandwagon" to "Bandwagon",
                    "appeal_to_fear" to "Appeal to Fear"
                ).map { (id, name) ->
                    Fallacy(
                        id = id,
                        name = name,
                        description = "See string resource: fallacy_${id}_description",
                        example = "See string resource: fallacy_${id}_example",
                        category = "",
                        isCustom = false,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    )
                }

                insertFallacies(defaultFallacies)
                Timber.i("Successfully inserted ${defaultFallacies.size} default fallacies")
            } else {
                Timber.d("Fallacies already exist in database (count: $count). Skipping initialization.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to ensure default fallacies exist")
            // Don't throw - allow the app to continue even if this fails
        }
    }
}
