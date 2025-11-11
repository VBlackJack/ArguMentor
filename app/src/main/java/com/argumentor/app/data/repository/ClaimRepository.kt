package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.model.Claim
import com.argumentor.app.util.FingerprintUtils
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
 *
 * SECURITY: Validates topic IDs and fallacy IDs to prevent SQL injection through json_each()
 */
@Singleton
class ClaimRepository @Inject constructor(
    private val claimDao: ClaimDao
) {
    companion object {
        /**
         * Valid ID format: UUID v4 pattern (alphanumeric and hyphens only)
         * This prevents SQL injection through json_each() queries
         */
        private val VALID_ID_PATTERN = "[a-zA-Z0-9-]+".toRegex()
    }

    /**
     * Validates that all IDs in a list match the expected UUID format.
     * @throws IllegalArgumentException if any ID contains invalid characters
     */
    private fun validateIds(ids: List<String>, fieldName: String) {
        ids.forEach { id ->
            require(id.matches(VALID_ID_PATTERN)) {
                "Invalid $fieldName format: '$id'. IDs must contain only alphanumeric characters and hyphens."
            }
        }
    }

    /**
     * Validates a single ID matches the expected UUID format.
     * @throws IllegalArgumentException if the ID contains invalid characters
     */
    private fun validateId(id: String, fieldName: String) {
        require(id.matches(VALID_ID_PATTERN)) {
            "Invalid $fieldName format: '$id'. IDs must contain only alphanumeric characters and hyphens."
        }
    }
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
     * @throws IllegalArgumentException if topicId format is invalid
     */
    suspend fun getClaimsForTopic(topicId: String): List<Claim> {
        validateId(topicId, "topicId")
        return claimDao.getClaimsByTopicId(topicId)
    }

    /**
     * Retrieves all claims associated with a specific fallacy.
     * @param fallacyId The fallacy identifier
     * @return List of claims containing the fallacy
     * @throws IllegalArgumentException if fallacyId format is invalid
     */
    suspend fun getClaimsForFallacy(fallacyId: String): List<Claim> {
        validateId(fallacyId, "fallacyId")
        return claimDao.getClaimsByFallacyId(fallacyId)
    }

    /**
     * Observes all claims associated with a specific fallacy.
     * @param fallacyId The fallacy identifier
     * @return Flow emitting list of claims containing the fallacy, updated automatically when data changes
     */
    fun observeClaimsForFallacy(fallacyId: String): Flow<List<Claim>> = claimDao.observeClaimsByFallacyId(fallacyId)

    /**
     * Inserts a new claim into the database.
     * Automatically generates and adds fingerprint for duplicate detection.
     * Validates that all topic and fallacy IDs are in the correct format.
     * @param claim The claim to insert
     * @throws IllegalArgumentException if any topic ID or fallacy ID format is invalid
     */
    suspend fun insertClaim(claim: Claim) {
        // Validate IDs before insertion to prevent SQL injection through json_each()
        validateIds(claim.topics, "topic ID")
        validateIds(claim.fallacyIds, "fallacy ID")

        val fingerprint = FingerprintUtils.generateClaimFingerprint(claim)
        claimDao.insertClaim(claim.copy(claimFingerprint = fingerprint))
    }

    /**
     * Updates an existing claim in the database.
     * Automatically regenerates fingerprint to reflect changes.
     * Validates that all topic and fallacy IDs are in the correct format.
     * @param claim The claim to update (must have existing ID)
     * @throws IllegalArgumentException if any topic ID or fallacy ID format is invalid
     */
    suspend fun updateClaim(claim: Claim) {
        // Validate IDs before update to prevent SQL injection through json_each()
        validateIds(claim.topics, "topic ID")
        validateIds(claim.fallacyIds, "fallacy ID")

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
