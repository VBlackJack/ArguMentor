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
    fun getEvidencesByClaimId(claimId: String): Flow<List<Evidence>> =
        evidenceDao.getEvidencesByClaimId(claimId)

    suspend fun getEvidenceById(evidenceId: String): Evidence? =
        evidenceDao.getEvidenceById(evidenceId)

    suspend fun insertEvidence(evidence: Evidence) =
        evidenceDao.insertEvidence(evidence)

    suspend fun updateEvidence(evidence: Evidence) =
        evidenceDao.updateEvidence(evidence)

    suspend fun deleteEvidence(evidence: Evidence) =
        evidenceDao.deleteEvidence(evidence)
}
