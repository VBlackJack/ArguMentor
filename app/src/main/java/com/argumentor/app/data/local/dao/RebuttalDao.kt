package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Rebuttal
import kotlinx.coroutines.flow.Flow

@Dao
interface RebuttalDao {
    @Query("SELECT * FROM rebuttals ORDER BY updatedAt DESC")
    fun getAllRebuttals(): Flow<List<Rebuttal>>

    @Query("SELECT * FROM rebuttals ORDER BY updatedAt DESC")
    suspend fun getAllRebuttalsSync(): List<Rebuttal>

    @Query("SELECT * FROM rebuttals WHERE claimId = :claimId ORDER BY updatedAt DESC")
    fun getRebuttalsByClaimId(claimId: String): Flow<List<Rebuttal>>

    @Query("SELECT * FROM rebuttals WHERE claimId = :claimId ORDER BY updatedAt DESC")
    suspend fun getRebuttalsByClaimIdSync(claimId: String): List<Rebuttal>

    @Query("SELECT * FROM rebuttals WHERE id = :rebuttalId")
    suspend fun getRebuttalById(rebuttalId: String): Rebuttal?

    @Query("SELECT * FROM rebuttals WHERE id = :rebuttalId")
    fun observeRebuttalById(rebuttalId: String): Flow<Rebuttal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRebuttal(rebuttal: Rebuttal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRebuttals(rebuttals: List<Rebuttal>)

    @Update
    suspend fun updateRebuttal(rebuttal: Rebuttal)

    @Delete
    suspend fun deleteRebuttal(rebuttal: Rebuttal)

    @Query("DELETE FROM rebuttals WHERE id = :rebuttalId")
    suspend fun deleteRebuttalById(rebuttalId: String)

    @Query("DELETE FROM rebuttals WHERE claimId = :claimId")
    suspend fun deleteRebuttalsByClaimId(claimId: String)

    // Full-text search
    @Query("""
        SELECT rebuttals.* FROM rebuttals
        JOIN rebuttals_fts ON rebuttals.rowid = rebuttals_fts.rowid
        WHERE rebuttals_fts MATCH :query
        ORDER BY updatedAt DESC
    """)
    fun searchRebuttalsFts(query: String): Flow<List<Rebuttal>>

    // Fallback search using LIKE (for when FTS query contains invalid operators)
    // SECURITY FIX (SEC-004): Added ESCAPE '\' clause to prevent wildcard injection
    @Query("""
        SELECT * FROM rebuttals
        WHERE text LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY updatedAt DESC
    """)
    fun searchRebuttalsLike(query: String): Flow<List<Rebuttal>>

    @Query("SELECT COUNT(*) FROM rebuttals")
    suspend fun getRebuttalCount(): Int
}
