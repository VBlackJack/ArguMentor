package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.SourceDao
import com.argumentor.app.data.model.Source
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SourceRepository @Inject constructor(
    private val sourceDao: SourceDao
) {
    fun getAllSources(): Flow<List<Source>> = sourceDao.getAllSources()

    fun getSourceById(sourceId: String): Flow<Source?> =
        sourceDao.observeSourceById(sourceId)

    suspend fun getSourceByIdSync(sourceId: String): Source? =
        sourceDao.getSourceById(sourceId)

    suspend fun insertSource(source: Source) =
        sourceDao.insertSource(source)

    suspend fun updateSource(source: Source) =
        sourceDao.updateSource(source)

    suspend fun deleteSource(source: Source) =
        sourceDao.deleteSource(source)

    suspend fun deleteSourceById(sourceId: String) =
        sourceDao.deleteSourceById(sourceId)

    /**
     * Search sources using FTS with automatic fallback to LIKE search if FTS fails.
     */
    fun searchSources(query: String): Flow<List<Source>> {
        return searchWithFtsFallback(
            query = query,
            ftsSearch = { sourceDao.searchSourcesFts(it) },
            likeSearch = { sourceDao.searchSourcesLike(it) }
        )
    }
}
