package com.argumentor.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.argumentor.app.data.local.dao.TopicDao
import com.argumentor.app.data.model.Topic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopicDaoTest {

    private lateinit var database: ArguMentorDatabase
    private lateinit var topicDao: TopicDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ArguMentorDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        topicDao = database.topicDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveTopic() = runBlocking {
        val topic = Topic(
            title = "Test Topic",
            summary = "Test Summary",
            posture = Topic.Posture.NEUTRAL_CRITICAL,
            tags = listOf("test", "kotlin")
        )

        topicDao.insertTopic(topic)

        val retrieved = topicDao.getTopicById(topic.id)
        assertNotNull(retrieved)
        assertEquals(topic.title, retrieved?.title)
        assertEquals(topic.summary, retrieved?.summary)
        assertEquals(topic.posture, retrieved?.posture)
        assertEquals(topic.tags, retrieved?.tags)
    }

    @Test
    fun getAllTopics() = runBlocking {
        val topic1 = Topic(title = "Topic 1", summary = "Summary 1")
        val topic2 = Topic(title = "Topic 2", summary = "Summary 2")

        topicDao.insertTopic(topic1)
        topicDao.insertTopic(topic2)

        val topics = topicDao.getAllTopics().first()
        assertEquals(2, topics.size)
    }

    @Test
    fun updateTopic() = runBlocking {
        val topic = Topic(title = "Original", summary = "Original Summary")
        topicDao.insertTopic(topic)

        val updated = topic.copy(title = "Updated", summary = "Updated Summary")
        topicDao.updateTopic(updated)

        val retrieved = topicDao.getTopicById(topic.id)
        assertEquals("Updated", retrieved?.title)
        assertEquals("Updated Summary", retrieved?.summary)
    }

    @Test
    fun deleteTopic() = runBlocking {
        val topic = Topic(title = "To Delete", summary = "Will be deleted")
        topicDao.insertTopic(topic)

        topicDao.deleteTopic(topic)

        val retrieved = topicDao.getTopicById(topic.id)
        assertNull(retrieved)
    }

    @Test
    fun searchTopics() = runBlocking {
        val topic1 = Topic(title = "Android Development", summary = "Learn Android")
        val topic2 = Topic(title = "iOS Development", summary = "Learn iOS")
        val topic3 = Topic(title = "Web Development", summary = "Learn Web with Android Studio")

        topicDao.insertTopic(topic1)
        topicDao.insertTopic(topic2)
        topicDao.insertTopic(topic3)

        val results = topicDao.searchTopicsLike("Android").first()
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == topic1.id })
        assertTrue(results.any { it.id == topic3.id })
    }

    @Test
    fun getTopicCount() = runBlocking {
        assertEquals(0, topicDao.getTopicCount())

        topicDao.insertTopic(Topic(title = "Topic 1", summary = "Summary 1"))
        topicDao.insertTopic(Topic(title = "Topic 2", summary = "Summary 2"))

        assertEquals(2, topicDao.getTopicCount())
    }
}
