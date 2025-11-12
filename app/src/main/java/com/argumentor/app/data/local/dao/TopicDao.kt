package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.flow.Flow

/**
 * Data class for topic statistics returned by SQL JOIN queries.
 * Used to avoid N+1 query patterns.
 */
data class TopicWithClaimCount(
    @Embedded val topic: Topic,
    @ColumnInfo(name = "claimCount") val claimCount: Int
)

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

    /**
     * Optimized query to get topics with claim counts using a single JOIN query.
     * This prevents N+1 query patterns where we'd load topics first, then query
     * claims for each topic separately.
     *
     * PERFORMANCE IMPROVEMENT (PERF-001):
     * - Single SQL query with JOIN instead of 1 + N queries
     * - Much faster for large databases
     * - Reduces database I/O from O(n) to O(1)
     *
     * @param limit Maximum number of topics to return (sorted by claim count DESC)
     * @return List of topics with their claim counts
     */
    @Query("""
        SELECT topics.*, COUNT(claims.id) as claimCount
        FROM topics
        LEFT JOIN claims ON claims.topicId = topics.id
        GROUP BY topics.id
        ORDER BY claimCount DESC
        LIMIT :limit
    """)
    suspend fun getTopicsWithClaimCount(limit: Int): List<TopicWithClaimCount>
}
