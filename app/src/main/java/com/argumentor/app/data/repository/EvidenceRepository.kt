package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.EvidenceDao
import com.argumentor.app.data.model.Evidence
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvidenceRepository @Inject constructor(
    private val evidenceDao: EvidenceDao
) {
    fun getAllEvidences(): Flow<List<Evidence>> =
        evidenceDao.getAllEvidences()

    fun getEvidencesByClaimId(claimId: String): Flow<List<Evidence>> =
        evidenceDao.getEvidencesByClaimId(claimId)

    fun getEvidencesBySourceId(sourceId: String): Flow<List<Evidence>> =
        evidenceDao.getEvidencesBySourceId(sourceId)

    suspend fun getEvidenceById(evidenceId: String): Evidence? =
        evidenceDao.getEvidenceById(evidenceId)

    suspend fun insertEvidence(evidence: Evidence) =
        evidenceDao.insertEvidence(evidence)

    suspend fun updateEvidence(evidence: Evidence) =
        evidenceDao.updateEvidence(evidence)

    suspend fun deleteEvidence(evidence: Evidence) =
        evidenceDao.deleteEvidence(evidence)

    /**
     * Search evidences using FTS with automatic fallback to LIKE search if FTS fails.
     */
    fun searchEvidences(query: String): Flow<List<Evidence>> {
        return searchWithFtsFallback(
            query = query,
            ftsSearch = { evidenceDao.searchEvidencesFts(it) },
            likeSearch = { evidenceDao.searchEvidencesLike(it) }
        )
    }
}
