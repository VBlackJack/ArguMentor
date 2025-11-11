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

    suspend fun insertRebuttal(rebuttal: Rebuttal) =
        rebuttalDao.insertRebuttal(rebuttal)

    suspend fun updateRebuttal(rebuttal: Rebuttal) =
        rebuttalDao.updateRebuttal(rebuttal)

    suspend fun deleteRebuttal(rebuttal: Rebuttal) =
        rebuttalDao.deleteRebuttal(rebuttal)

    suspend fun deleteRebuttalsByClaimId(claimId: String) =
        rebuttalDao.deleteRebuttalsByClaimId(claimId)

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
