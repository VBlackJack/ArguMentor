package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Fallacy
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Fallacy entities.
 * Provides CRUD operations and search functionality for logical fallacies.
 */
@Dao
interface FallacyDao {
    /**
     * Get all fallacies ordered by name.
     */
    @Query("SELECT * FROM fallacies ORDER BY name ASC")
    fun getAllFallacies(): Flow<List<Fallacy>>

    /**
     * Get all fallacies synchronously.
     */
    @Query("SELECT * FROM fallacies ORDER BY name ASC")
    suspend fun getAllFallaciesSync(): List<Fallacy>

    /**
     * Get a fallacy by its ID.
     */
    @Query("SELECT * FROM fallacies WHERE id = :fallacyId")
    suspend fun getFallacyById(fallacyId: String): Fallacy?

    /**
     * Observe a fallacy by its ID.
     */
    @Query("SELECT * FROM fallacies WHERE id = :fallacyId")
    fun observeFallacyById(fallacyId: String): Flow<Fallacy?>

    /**
     * Search fallacies by name or description using LIKE.
     * SECURITY FIX (SEC-004): Added ESCAPE '\' clause to prevent wildcard injection.
     */
    @Query("""
        SELECT * FROM fallacies
        WHERE name LIKE '%' || :query || '%' ESCAPE '\'
        OR description LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY name ASC
    """)
    fun searchFallacies(query: String): Flow<List<Fallacy>>

    /**
     * Get fallacies by category.
     */
    @Query("SELECT * FROM fallacies WHERE category = :category ORDER BY name ASC")
    fun getFallaciesByCategory(category: String): Flow<List<Fallacy>>

    /**
     * Get all custom (user-created) fallacies.
     */
    @Query("SELECT * FROM fallacies WHERE isCustom = 1 ORDER BY name ASC")
    fun getCustomFallacies(): Flow<List<Fallacy>>

    /**
     * Get all pre-loaded (non-custom) fallacies.
     */
    @Query("SELECT * FROM fallacies WHERE isCustom = 0 ORDER BY name ASC")
    fun getPreloadedFallacies(): Flow<List<Fallacy>>

    /**
     * Insert a single fallacy.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFallacy(fallacy: Fallacy)

    /**
     * Insert multiple fallacies.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFallacies(fallacies: List<Fallacy>)

    /**
     * Update a fallacy.
     */
    @Update
    suspend fun updateFallacy(fallacy: Fallacy)

    /**
     * Delete a fallacy.
     */
    @Delete
    suspend fun deleteFallacy(fallacy: Fallacy)

    /**
     * Delete a fallacy by ID.
     */
    @Query("DELETE FROM fallacies WHERE id = :fallacyId")
    suspend fun deleteFallacyById(fallacyId: String)

    /**
     * Get the count of all fallacies.
     */
    @Query("SELECT COUNT(*) FROM fallacies")
    suspend fun getFallacyCount(): Int

    /**
     * Get the count of custom fallacies.
     */
    @Query("SELECT COUNT(*) FROM fallacies WHERE isCustom = 1")
    suspend fun getCustomFallacyCount(): Int

    /**
     * Check if a fallacy exists by ID.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM fallacies WHERE id = :fallacyId LIMIT 1)")
    suspend fun fallacyExists(fallacyId: String): Boolean
}
