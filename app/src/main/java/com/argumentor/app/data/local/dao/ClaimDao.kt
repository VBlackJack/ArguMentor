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

    @Query("SELECT * FROM claims WHERE :topicId IN (SELECT value FROM json_each(topics))")
    suspend fun getClaimsForTopic(topicId: String): List<Claim>

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

    // Full-text search
    @Query("""
        SELECT claims.* FROM claims
        JOIN claims_fts ON claims.rowid = claims_fts.rowid
        WHERE claims_fts MATCH :query
        ORDER BY updatedAt DESC
    """)
    fun searchClaimsFts(query: String): Flow<List<Claim>>

    @Query("SELECT * FROM claims WHERE claimFingerprint = :fingerprint")
    suspend fun getClaimByFingerprint(fingerprint: String): Claim?

    @Query("SELECT COUNT(*) FROM claims")
    suspend fun getClaimCount(): Int
}
