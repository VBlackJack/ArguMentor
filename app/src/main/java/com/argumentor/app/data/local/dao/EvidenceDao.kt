package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Evidence
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidences ORDER BY updatedAt DESC")
    fun getAllEvidences(): Flow<List<Evidence>>

    @Query("SELECT * FROM evidences ORDER BY updatedAt DESC")
    suspend fun getAllEvidencesSync(): List<Evidence>

    @Query("SELECT * FROM evidences WHERE claimId = :claimId ORDER BY updatedAt DESC")
    fun getEvidencesByClaimId(claimId: String): Flow<List<Evidence>>

    @Query("SELECT * FROM evidences WHERE claimId = :claimId ORDER BY updatedAt DESC")
    suspend fun getEvidencesByClaimIdSync(claimId: String): List<Evidence>

    /**
     * Note: Evidence entities are linked to claims, not to rebuttals.
     * This design decision was made because:
     * - Evidence supports claims with factual backing
     * - Rebuttals counter claims, but evidence provides the foundation
     * - Simplifies the data model and avoids deep nesting
     */

    @Query("SELECT * FROM evidences WHERE id = :evidenceId")
    suspend fun getEvidenceById(evidenceId: String): Evidence?

    @Query("SELECT * FROM evidences WHERE id = :evidenceId")
    fun observeEvidenceById(evidenceId: String): Flow<Evidence?>

    @Query("SELECT * FROM evidences WHERE sourceId = :sourceId ORDER BY updatedAt DESC")
    fun getEvidencesBySourceId(sourceId: String): Flow<List<Evidence>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: Evidence)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidences(evidences: List<Evidence>)

    @Update
    suspend fun updateEvidence(evidence: Evidence)

    @Delete
    suspend fun deleteEvidence(evidence: Evidence)

    @Query("DELETE FROM evidences WHERE id = :evidenceId")
    suspend fun deleteEvidenceById(evidenceId: String)

    @Query("DELETE FROM evidences WHERE claimId = :claimId")
    suspend fun deleteEvidencesByClaimId(claimId: String)

    /**
     * Full-text search on evidences using FTS4 index.
     * Searches in content field.
     * @param query Search query (supports FTS4 operators like OR, AND, *)
     * @return Flow of matching evidences ordered by updated date
     */
    @Query("""
        SELECT evidences.* FROM evidences
        JOIN evidences_fts ON evidences.rowid = evidences_fts.rowid
        WHERE evidences_fts MATCH :query
        ORDER BY updatedAt DESC
    """)
    fun searchEvidencesFts(query: String): Flow<List<Evidence>>

    /**
     * Fallback search using LIKE (for when FTS query contains invalid operators).
     * Searches in content field.
     * SECURITY FIX (SEC-004): Added ESCAPE '\' clause to prevent wildcard injection
     * @param query Search query string
     * @return Flow of matching evidences ordered by updated date
     */
    @Query("""
        SELECT * FROM evidences
        WHERE content LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY updatedAt DESC
    """)
    fun searchEvidencesLike(query: String): Flow<List<Evidence>>

    @Query("SELECT COUNT(*) FROM evidences")
    suspend fun getEvidenceCount(): Int

    /**
     * Get evidences for multiple claims in a single query.
     * PERF-003 FIX: Prevents N+1 query pattern.
     */
    @Query("SELECT * FROM evidences WHERE claimId IN (:claimIds) ORDER BY updatedAt DESC")
    suspend fun getEvidencesByClaimIds(claimIds: List<String>): List<Evidence>
}
