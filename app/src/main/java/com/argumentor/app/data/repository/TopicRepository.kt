package com.argumentor.app.data.repository

import com.argumentor.app.data.local.dao.TopicDao
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopicRepository @Inject constructor(
    private val topicDao: TopicDao
) {
    fun getAllTopics(): Flow<List<Topic>> = topicDao.getAllTopics()

    fun getTopicById(topicId: String): Flow<Topic?> = topicDao.observeTopicById(topicId)

    suspend fun getTopicByIdSync(topicId: String): Topic? = topicDao.getTopicById(topicId)

    suspend fun insertTopic(topic: Topic) = topicDao.insertTopic(topic)

    suspend fun updateTopic(topic: Topic) = topicDao.updateTopic(topic)

    suspend fun deleteTopic(topic: Topic) = topicDao.deleteTopic(topic)

    fun searchTopics(query: String): Flow<List<Topic>> = topicDao.searchTopics(query)
}
