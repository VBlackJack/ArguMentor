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
     *
     * Note: This uses MANUAL cascade deletion instead of database Foreign Keys because:
     * 1. Topic-Claim relationship is Many-to-Many (stored as JSON array in claims.topics)
     * 2. Question.targetId is polymorphic (can reference Topic OR Claim)
     * 3. Room/SQLite cannot enforce FK CASCADE on these complex relationship patterns
     *
     * Alternative considered: Creating junction tables (Topic_Claim, Question_Target)
     * with proper FKs, but this would significantly complicate the schema and queries.
     * Current approach provides better query performance for the common use case.
     */
    suspend fun deleteTopicById(topicId: String) {
        // 1. Get all claims for this topic to find associated questions
        val claims = claimDao.getClaimsByTopicId(topicId)

        // 2. Delete questions linked to each claim
        claims.forEach { claim ->
            questionDao.deleteQuestionsByTargetId(claim.id)
        }

        // 3. Delete all claims for this topic using json_each()
        //    (Rebuttals and Evidence will be deleted automatically via FK CASCADE)
        claimDao.deleteClaimsByTopicId(topicId)

        // 4. Delete questions linked to the topic itself
        questionDao.deleteQuestionsByTargetId(topicId)

        // 5. Finally, delete the topic
        topicDao.deleteTopicById(topicId)
    }

    /**
     * Search topics using FTS with automatic fallback to LIKE search if FTS fails.
     */
    fun searchTopics(query: String): Flow<List<Topic>> {
        return searchWithFtsFallback(
            query = query,
            ftsSearch = { topicDao.searchTopicsFts(it) },
            likeSearch = { topicDao.searchTopicsLike(it) }
        )
    }
}
