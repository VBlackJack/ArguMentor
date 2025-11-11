package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Source
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY createdAt DESC")
    fun getAllSources(): Flow<List<Source>>

    @Query("SELECT * FROM sources ORDER BY createdAt DESC")
    suspend fun getAllSourcesSync(): List<Source>

    @Query("SELECT * FROM sources WHERE id = :sourceId")
    suspend fun getSourceById(sourceId: String): Source?

    @Query("SELECT * FROM sources WHERE id = :sourceId")
    fun observeSourceById(sourceId: String): Flow<Source?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: Source)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<Source>)

    @Update
    suspend fun updateSource(source: Source)

    @Delete
    suspend fun deleteSource(source: Source)

    @Query("DELETE FROM sources WHERE id = :sourceId")
    suspend fun deleteSourceById(sourceId: String)

    @Query("SELECT * FROM sources WHERE title LIKE '%' || :query || '%'")
    fun searchSources(query: String): Flow<List<Source>>

    /**
     * Full-text search on sources using FTS4 index.
     * Searches in title and citation fields with relevance ranking.
     * @param query Search query (supports FTS4 operators like OR, AND, *)
     * @return Flow of matching sources ordered by title
     */
    @Query("""
        SELECT sources.* FROM sources
        JOIN sources_fts ON sources.rowid = sources_fts.rowid
        WHERE sources_fts MATCH :query
        ORDER BY sources.title
    """)
    fun searchSourcesFts(query: String): Flow<List<Source>>

    @Query("SELECT COUNT(*) FROM sources")
    suspend fun getSourceCount(): Int
}
