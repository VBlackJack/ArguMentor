package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Claim
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims ORDER BY updatedAt DESC")
    fun getAllClaims(): Flow<List<Claim>>

    @Query("SELECT * FROM claims ORDER BY updatedAt DESC")
    suspend fun getAllClaimsSync(): List<Claim>

    @Query("SELECT * FROM claims WHERE id = :claimId")
    suspend fun getClaimById(claimId: String): Claim?

    /**
     * Bulk query to get multiple claims by their IDs.
     *
     * PERFORMANCE: Prevents N+1 query problem when loading multiple claims.
     * Instead of making N separate queries for N claims, makes 1 query with IN clause.
     *
     * @param claimIds List of claim IDs to fetch
     * @return List of Claims matching the provided IDs
     */
    @Query("SELECT * FROM claims WHERE id IN (:claimIds) ORDER BY updatedAt DESC")
    suspend fun getClaimsByIds(claimIds: List<String>): List<Claim>

    @Query("SELECT * FROM claims WHERE :topicId IN (SELECT value FROM json_each(topics))")
    suspend fun getClaimsByTopicId(topicId: String): List<Claim>

    @Query("SELECT * FROM claims WHERE :fallacyId IN (SELECT value FROM json_each(fallacyIds))")
    suspend fun getClaimsByFallacyId(fallacyId: String): List<Claim>

    @Query("SELECT * FROM claims WHERE :fallacyId IN (SELECT value FROM json_each(fallacyIds))")
    fun observeClaimsByFallacyId(fallacyId: String): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE id = :claimId")
    fun observeClaimById(claimId: String): Flow<Claim?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: Claim)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaims(claims: List<Claim>)

    @Update
    suspend fun updateClaim(claim: Claim)

    @Delete
    suspend fun deleteClaim(claim: Claim)

    @Query("DELETE FROM claims WHERE id = :claimId")
    suspend fun deleteClaimById(claimId: String)

    /**
     * Delete all claims linked to a specific topic.
     *
     * Note: Uses json_each() because the topics field stores a JSON array of topic IDs.
     * This is more performant than maintaining a separate junction table for the
     * many-to-many Topic-Claim relationship, especially for read-heavy operations
     * (which are the majority in this app).
     *
     * Trade-off: Query complexity vs schema simplicity and read performance.
     */
    @Query("DELETE FROM claims WHERE :topicId IN (SELECT value FROM json_each(topics))")
    suspend fun deleteClaimsByTopicId(topicId: String)

    // Full-text search
    @Query("""
        SELECT claims.* FROM claims
        JOIN claims_fts ON claims.rowid = claims_fts.rowid
        WHERE claims_fts MATCH :query
        ORDER BY updatedAt DESC
    """)
    fun searchClaimsFts(query: String): Flow<List<Claim>>

    // Fallback search using LIKE (for when FTS query contains invalid operators)
    // SECURITY FIX (SEC-004): Added ESCAPE '\' clause to prevent wildcard injection
    @Query("""
        SELECT * FROM claims
        WHERE text LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY updatedAt DESC
    """)
    fun searchClaimsLike(query: String): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE claimFingerprint = :fingerprint")
    suspend fun getClaimByFingerprint(fingerprint: String): Claim?

    @Query("SELECT COUNT(*) FROM claims")
    suspend fun getClaimCount(): Int
}
