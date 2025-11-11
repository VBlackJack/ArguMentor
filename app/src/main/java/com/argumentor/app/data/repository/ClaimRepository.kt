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

/**
 * Repository for managing claim entities.
 * Handles CRUD operations, search functionality, and fingerprint generation for claims.
 */
@Singleton
class ClaimRepository @Inject constructor(
    private val claimDao: ClaimDao
) {
    /**
     * Observes all claims in the database.
     * @return Flow emitting list of all claims, updated automatically when data changes
     */
    fun getAllClaims(): Flow<List<Claim>> = claimDao.getAllClaims()

    /**
     * Observes a specific claim by ID.
     * @param claimId The unique identifier of the claim
     * @return Flow emitting the claim or null if not found, updated automatically when data changes
     */
    fun getClaimById(claimId: String): Flow<Claim?> = claimDao.observeClaimById(claimId)

    /**
     * Retrieves a claim by ID (one-time fetch, not observed).
     * @param claimId The unique identifier of the claim
     * @return The claim or null if not found
     */
    suspend fun getClaimByIdSync(claimId: String): Claim? = claimDao.getClaimById(claimId)

    /**
     * Retrieves all claims associated with a specific topic.
     * @param topicId The topic identifier
     * @return List of claims belonging to the topic
     */
    suspend fun getClaimsForTopic(topicId: String): List<Claim> = claimDao.getClaimsForTopic(topicId)

    /**
     * Retrieves all claims associated with a specific fallacy.
     * @param fallacyId The fallacy identifier
     * @return List of claims containing the fallacy
     */
    suspend fun getClaimsForFallacy(fallacyId: String): List<Claim> = claimDao.getClaimsForFallacy(fallacyId)

    /**
     * Observes all claims associated with a specific fallacy.
     * @param fallacyId The fallacy identifier
     * @return Flow emitting list of claims containing the fallacy, updated automatically when data changes
     */
    fun observeClaimsForFallacy(fallacyId: String): Flow<List<Claim>> = claimDao.observeClaimsForFallacy(fallacyId)

    /**
     * Inserts a new claim into the database.
     * Automatically generates and adds fingerprint for duplicate detection.
     * @param claim The claim to insert
     */
    suspend fun insertClaim(claim: Claim) {
        val fingerprint = FingerprintUtils.generateClaimFingerprint(claim)
        claimDao.insertClaim(claim.copy(claimFingerprint = fingerprint))
    }

    /**
     * Updates an existing claim in the database.
     * Automatically regenerates fingerprint to reflect changes.
     * @param claim The claim to update (must have existing ID)
     */
    suspend fun updateClaim(claim: Claim) {
        val fingerprint = FingerprintUtils.generateClaimFingerprint(claim)
        claimDao.updateClaim(claim.copy(claimFingerprint = fingerprint))
    }

    /**
     * Deletes a claim from the database.
     * Cascading delete will remove associated evidences and rebuttals.
     * @param claim The claim to delete
     */
    suspend fun deleteClaim(claim: Claim) = claimDao.deleteClaim(claim)

    /**
     * Searches claims using full-text search with automatic fallback to LIKE search if FTS fails.
     * @param query The search query string
     * @return Flow emitting list of matching claims
     */
    fun searchClaims(query: String): Flow<List<Claim>> {
        return searchWithFtsFallback(
            query = query,
            ftsSearch = { claimDao.searchClaimsFts(it) },
            likeSearch = { claimDao.searchClaimsLike(it) }
        )
    }
}
