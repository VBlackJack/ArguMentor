package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.RebuttalDao
import com.argumentor.app.data.model.Rebuttal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing rebuttal entities.
 * Handles CRUD operations with robust error handling on all Flow operations.
 *
 * ROBUSTNESS: All Flows include error handling to prevent crashes and provide graceful degradation.
 */
@Singleton
class RebuttalRepository @Inject constructor(
    private val rebuttalDao: RebuttalDao
) {
    /**
     * Observes all rebuttals with error handling.
     * Returns empty list on errors to maintain UI stability.
     */
    fun getAllRebuttals(): Flow<List<Rebuttal>> =
        rebuttalDao.getAllRebuttals()
            .catch { e ->
                Timber.e(e, "Error loading all rebuttals")
                emit(emptyList())
            }

    /**
     * Observes rebuttals for a specific claim with error handling.
     * Returns empty list on errors to maintain UI stability.
     */
    fun getRebuttalsByClaimId(claimId: String): Flow<List<Rebuttal>> =
        rebuttalDao.getRebuttalsByClaimId(claimId)
            .catch { e ->
                Timber.e(e, "Error loading rebuttals for claim: $claimId")
                emit(emptyList())
            }

    suspend fun getRebuttalById(rebuttalId: String): Rebuttal? =
        rebuttalDao.getRebuttalById(rebuttalId)

    /**
     * Observes a single rebuttal by ID with error handling.
     * Returns null on errors.
     */
    fun observeRebuttalById(rebuttalId: String): Flow<Rebuttal?> =
        rebuttalDao.observeRebuttalById(rebuttalId)
            .catch { e ->
                Timber.e(e, "Error observing rebuttal: $rebuttalId")
                emit(null)
            }

    /**
     * Insert a rebuttal with error handling.
     *
     * ROBUSTNESS: Returns Result to allow callers to handle errors gracefully.
     * Previous version threw exceptions which could crash the app if unhandled.
     *
     * @return Result.success(Unit) if successful, Result.failure(exception) if error occurs
     */
    suspend fun insertRebuttal(rebuttal: Rebuttal): Result<Unit> =
        try {
            rebuttalDao.insertRebuttal(rebuttal)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert rebuttal: ${rebuttal.id}")
            Result.failure(e)
        }

    /**
     * Update a rebuttal with error handling.
     *
     * ROBUSTNESS: Returns Result to allow callers to handle errors gracefully.
     *
     * @return Result.success(Unit) if successful, Result.failure(exception) if error occurs
     */
    suspend fun updateRebuttal(rebuttal: Rebuttal): Result<Unit> =
        try {
            rebuttalDao.updateRebuttal(rebuttal)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update rebuttal: ${rebuttal.id}")
            Result.failure(e)
        }

    /**
     * Delete a rebuttal with error handling.
     *
     * ROBUSTNESS: Returns Result to allow callers to handle errors gracefully.
     *
     * @return Result.success(Unit) if successful, Result.failure(exception) if error occurs
     */
    suspend fun deleteRebuttal(rebuttal: Rebuttal): Result<Unit> =
        try {
            rebuttalDao.deleteRebuttal(rebuttal)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete rebuttal: ${rebuttal.id}")
            Result.failure(e)
        }

    /**
     * Delete all rebuttals for a claim with error handling.
     *
     * ROBUSTNESS: Returns Result to allow callers to handle errors gracefully.
     *
     * @return Result.success(Unit) if successful, Result.failure(exception) if error occurs
     */
    suspend fun deleteRebuttalsByClaimId(claimId: String): Result<Unit> =
        try {
            rebuttalDao.deleteRebuttalsByClaimId(claimId)
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete rebuttals for claim: $claimId")
            Result.failure(e)
        }

    /**
     * Search rebuttals using FTS with automatic fallback to LIKE search if FTS fails.
     */
    fun searchRebuttals(query: String): Flow<List<Rebuttal>> {
        return searchWithFtsFallback(
            query = query,
            ftsSearch = { rebuttalDao.searchRebuttalsFts(it) },
            likeSearch = { rebuttalDao.searchRebuttalsLike(it) }
        )
    }
}
