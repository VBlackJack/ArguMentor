package com.argumentor.app.data.local.dao

import androidx.room.*
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY updatedAt DESC")
    fun getAllTopics(): Flow<List<Topic>>

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

    @Query("SELECT * FROM topics WHERE title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%'")
    fun searchTopics(query: String): Flow<List<Topic>>

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun getTopicCount(): Int
}
