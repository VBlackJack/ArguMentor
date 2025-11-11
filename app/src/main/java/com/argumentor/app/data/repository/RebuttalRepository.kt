package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.RebuttalDao
import com.argumentor.app.data.model.Rebuttal
import com.argumentor.app.util.SearchUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RebuttalRepository @Inject constructor(
    private val rebuttalDao: RebuttalDao
) {
    fun getAllRebuttals(): Flow<List<Rebuttal>> =
        rebuttalDao.getAllRebuttals()

    fun getRebuttalsByClaimId(claimId: String): Flow<List<Rebuttal>> =
        rebuttalDao.getRebuttalsByClaimId(claimId)

    suspend fun getRebuttalById(rebuttalId: String): Rebuttal? =
        rebuttalDao.getRebuttalById(rebuttalId)

    fun observeRebuttalById(rebuttalId: String): Flow<Rebuttal?> =
        rebuttalDao.observeRebuttalById(rebuttalId)

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
