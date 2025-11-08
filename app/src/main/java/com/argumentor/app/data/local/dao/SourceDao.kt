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

    @Query("SELECT COUNT(*) FROM sources")
    suspend fun getSourceCount(): Int
}
