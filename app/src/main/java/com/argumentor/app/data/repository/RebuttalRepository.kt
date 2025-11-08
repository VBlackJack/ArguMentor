package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.RebuttalDao
import com.argumentor.app.data.model.Rebuttal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RebuttalRepository @Inject constructor(
    private val rebuttalDao: RebuttalDao
) {
    fun getRebuttalsByClaimId(claimId: String): Flow<List<Rebuttal>> =
        rebuttalDao.getRebuttalsByClaimId(claimId)

    suspend fun getRebuttalById(rebuttalId: String): Rebuttal? =
        rebuttalDao.getRebuttalById(rebuttalId)

    suspend fun insertRebuttal(rebuttal: Rebuttal) =
        rebuttalDao.insertRebuttal(rebuttal)

    suspend fun updateRebuttal(rebuttal: Rebuttal) =
        rebuttalDao.updateRebuttal(rebuttal)

    suspend fun deleteRebuttal(rebuttal: Rebuttal) =
        rebuttalDao.deleteRebuttal(rebuttal)

    fun searchRebuttals(query: String): Flow<List<Rebuttal>> =
        rebuttalDao.searchRebuttalsFts(query)
}
