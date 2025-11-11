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

    // Note: Evidence is linked to claims, not rebuttals
    // @Query("SELECT * FROM evidences WHERE rebuttalId = :rebuttalId ORDER BY updatedAt DESC")
    // suspend fun getEvidenceForRebuttal(rebuttalId: String): List<Evidence>

    @Query("SELECT * FROM evidences WHERE id = :evidenceId")
    suspend fun getEvidenceById(evidenceId: String): Evidence?

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

    @Query("SELECT COUNT(*) FROM evidences")
    suspend fun getEvidenceCount(): Int
}
