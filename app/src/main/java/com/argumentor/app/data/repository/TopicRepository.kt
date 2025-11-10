package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.local.dao.QuestionDao
import com.argumentor.app.data.local.dao.TopicDao
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepository @Inject constructor(
    private val topicDao: TopicDao,
    private val claimDao: ClaimDao,
    private val questionDao: QuestionDao
) {
    fun getAllTopics(): Flow<List<Topic>> = topicDao.getAllTopics()

    fun getTopicById(topicId: String): Flow<Topic?> = topicDao.observeTopicById(topicId)

    suspend fun getTopicByIdSync(topicId: String): Topic? = topicDao.getTopicById(topicId)

    suspend fun insertTopic(topic: Topic) = topicDao.insertTopic(topic)

    suspend fun updateTopic(topic: Topic) = topicDao.updateTopic(topic)

    /**
     * Delete a topic and all its associated content in cascade:
     * - Claims linked to this topic
     * - Questions linked to this topic or its claims
     * - Rebuttals and Evidence (automatically deleted via foreign key cascade)
     */
    suspend fun deleteTopic(topic: Topic) = deleteTopicById(topic.id)

    /**
     * Delete a topic and all its associated content in cascade:
     * - Claims linked to this topic
     * - Questions linked to this topic or its claims
     * - Rebuttals and Evidence (automatically deleted via foreign key cascade)
     */
    suspend fun deleteTopicById(topicId: String) {
        // 1. Get all claims for this topic to find associated questions
        val claims = claimDao.getClaimsForTopic(topicId)

        // 2. Delete questions linked to each claim
        claims.forEach { claim ->
            questionDao.deleteQuestionsByTargetId(claim.id)
        }

        // 3. Delete all claims for this topic
        //    (Rebuttals and Evidence will be deleted automatically via FK CASCADE)
        claimDao.deleteClaimsByTopicId(topicId)

        // 4. Delete questions linked to the topic itself
        questionDao.deleteQuestionsByTargetId(topicId)

        // 5. Finally, delete the topic
        topicDao.deleteTopicById(topicId)
    }

    fun searchTopics(query: String): Flow<List<Topic>> = topicDao.searchTopics(query)

    /**
     * Get topics filtered by tag and/or search query using SQL-based filtering.
     * Much more efficient than loading all topics and filtering in memory.
     *
     * @param tag Optional tag to filter by (null for no tag filter)
     * @param query Optional search text (null for no text filter)
     * @return Flow of filtered topics, sorted by last updated
     */
    fun getFilteredTopics(tag: String?, query: String?): Flow<List<Topic>> {
        return topicDao.getFilteredTopics(tag, query)
    }
}
