package com.argumentor.app.data.repository

import android.content.Context
import com.argumentor.app.data.constants.FallacyCatalog
import com.argumentor.app.data.local.dao.FallacyDao
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val fallacyDao: FallacyDao,
    @ApplicationContext private val context: Context
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

        // MEDIUM-005 FIX: Use imported function instead of fully qualified name
        // Update the updatedAt timestamp
        val updatedFallacy = fallacy.copy(
            updatedAt = getCurrentIsoTimestamp()
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
     * fallacies are always available with localized names.
     */
    suspend fun ensureDefaultFallaciesExist() {
        try {
            val count = getFallacyCount()

            // Use FallacyCatalog to get fallacies with localized names from string resources
            val catalogFallacies = FallacyCatalog.getFallacies(context)

            if (count == 0) {
                // Database is empty - insert all default fallacies
                Timber.d("No fallacies found in database. Inserting ${catalogFallacies.size} default fallacies with localized names...")

                val currentTime = getCurrentIsoTimestamp()
                val defaultFallacies = catalogFallacies.map { catalogFallacy ->
                    Fallacy(
                        id = catalogFallacy.id,
                        name = catalogFallacy.name,
                        description = catalogFallacy.description,
                        example = catalogFallacy.example,
                        category = "",
                        isCustom = false,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    )
                }

                insertFallacies(defaultFallacies)
                Timber.i("Successfully inserted ${defaultFallacies.size} default fallacies with localized names")
            } else {
                // Database has fallacies - update existing ones with localized names
                Timber.d("Fallacies exist in database (count: $count). Updating pre-loaded fallacies with localized names...")

                val existingFallacies = getAllFallaciesSync()
                var updatedCount = 0

                catalogFallacies.forEach { catalogFallacy ->
                    val existingFallacy = existingFallacies.find { it.id == catalogFallacy.id && !it.isCustom }
                    if (existingFallacy != null) {
                        // Update only if the name/description/example is different (avoid unnecessary updates)
                        if (existingFallacy.name != catalogFallacy.name ||
                            existingFallacy.description != catalogFallacy.description ||
                            existingFallacy.example != catalogFallacy.example) {

                            val updatedFallacy = existingFallacy.copy(
                                name = catalogFallacy.name,
                                description = catalogFallacy.description,
                                example = catalogFallacy.example,
                                updatedAt = getCurrentIsoTimestamp()
                            )
                            updateFallacy(updatedFallacy)
                            updatedCount++
                        }
                    }
                }

                if (updatedCount > 0) {
                    Timber.i("Updated $updatedCount pre-loaded fallacies with localized names")
                } else {
                    Timber.d("All pre-loaded fallacies already have correct localized names")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to ensure default fallacies exist")
            // Don't throw - allow the app to continue even if this fails
        }
    }
}
