package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.model.Claim
import com.argumentor.app.util.FingerprintUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaimRepository @Inject constructor(
    private val claimDao: ClaimDao
) {
    fun getAllClaims(): Flow<List<Claim>> = claimDao.getAllClaims()

    fun getClaimById(claimId: String): Flow<Claim?> = claimDao.observeClaimById(claimId)

    suspend fun getClaimByIdSync(claimId: String): Claim? = claimDao.getClaimById(claimId)

    suspend fun insertClaim(claim: Claim) {
        val fingerprint = FingerprintUtils.generateClaimFingerprint(claim)
        claimDao.insertClaim(claim.copy(claimFingerprint = fingerprint))
    }

    suspend fun updateClaim(claim: Claim) {
        val fingerprint = FingerprintUtils.generateClaimFingerprint(claim)
        claimDao.updateClaim(claim.copy(claimFingerprint = fingerprint))
    }

    suspend fun deleteClaim(claim: Claim) = claimDao.deleteClaim(claim)

    fun searchClaims(query: String): Flow<List<Claim>> = claimDao.searchClaimsFts(query)
}
