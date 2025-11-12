package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY updatedAt DESC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics ORDER BY updatedAt DESC")
    suspend fun getAllTopicsSync(): List<Topic>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: String): Topic?

    @Query("SELECT * FROM topics WHERE id = :topicId")
    fun observeTopicById(topicId: String): Flow<Topic?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<Topic>)

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopicById(topicId: String)

    /**
     * Full-text search on topics using FTS4 index.
     * Searches in title and summary fields.
     * @param query Search query (supports FTS4 operators like OR, AND, *)
     * @return Flow of matching topics ordered by updated date
     */
    @Query("""
        SELECT topics.* FROM topics
        JOIN topics_fts ON topics.rowid = topics_fts.rowid
        WHERE topics_fts MATCH :query
        ORDER BY updatedAt DESC
    """)
    fun searchTopicsFts(query: String): Flow<List<Topic>>

    /**
     * Fallback search using LIKE (for when FTS query contains invalid operators).
     * Searches in title and summary fields.
     * SECURITY FIX (SEC-004): Added ESCAPE '\' clause to prevent wildcard injection.
     * @param query Search query string (wildcards are escaped by SearchUtils)
     * @return Flow of matching topics ordered by updated date
     */
    @Query("""
        SELECT * FROM topics
        WHERE title LIKE '%' || :query || '%' ESCAPE '\'
           OR summary LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY updatedAt DESC
    """)
    fun searchTopicsLike(query: String): Flow<List<Topic>>

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun getTopicCount(): Int

    // Aggregated queries for statistics (prevents OOM by not loading all data in memory)

    @Query("SELECT COUNT(*) FROM topics WHERE posture = :posture")
    suspend fun getTopicCountByPosture(posture: Topic.Posture): Int
}
