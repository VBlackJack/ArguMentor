package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.model.Claim
import com.argumentor.app.util.FingerprintUtils
import com.argumentor.app.util.SearchUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
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

    /**
     * Search claims using FTS with automatic fallback to LIKE search if FTS fails.
     */
    fun searchClaims(query: String): Flow<List<Claim>> {
        val sanitizedQuery = SearchUtils.sanitizeLikeQuery(query)

        return if (SearchUtils.isSafeFtsQuery(query)) {
            // Try FTS first
            claimDao.searchClaimsFts(query).catch { error ->
                // If FTS fails (e.g., invalid query syntax), fall back to LIKE
                emitAll(claimDao.searchClaimsLike(sanitizedQuery))
            }
        } else {
            // Query looks unsafe for FTS, use LIKE directly
            claimDao.searchClaimsLike(sanitizedQuery)
        }
    }
}
