package com.argumentor.app.data.repository

import app.cash.turbine.test
import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.local.dao.QuestionDao
import com.argumentor.app.data.local.dao.TopicDao
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Topic
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for TopicRepository.
 *
 * Note: Cascade delete uses database.withTransaction() which requires
 * instrumented tests with a real database. Basic DAO operations can be
 * tested with mocks. Full cascade delete integration tests are in androidTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TopicRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: ArguMentorDatabase
    private lateinit var topicDao: TopicDao
    private lateinit var claimDao: ClaimDao
    private lateinit var questionDao: QuestionDao
    private lateinit var repository: TopicRepository

    private val testTimestamp = "2024-01-15T10:30:00Z"

    private val testTopic = Topic(
        id = "topic-1",
        title = "Test Topic",
        summary = "Test Summary",
        createdAt = testTimestamp,
        updatedAt = testTimestamp
    )

    private val testClaim = Claim(
        id = "claim-1",
        text = "Test Claim",
        topics = listOf("topic-1"),
        stance = Claim.Stance.PRO,
        strength = Claim.Strength.MEDIUM,
        createdAt = testTimestamp,
        updatedAt = testTimestamp
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        database = mock()
        topicDao = mock()
        claimDao = mock()
        questionDao = mock()

        repository = TopicRepository(database, topicDao, claimDao, questionDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =====================
    // READ TESTS
    // =====================

    @Test
    fun `getAllTopics returns flow from dao`() = runTest {
        val topics = listOf(testTopic)
        whenever(topicDao.getAllTopics()).thenReturn(flowOf(topics))

        repository.getAllTopics().test {
            assertThat(awaitItem()).isEqualTo(topics)
            awaitComplete()
        }
    }

    @Test
    fun `getTopicById returns flow from dao`() = runTest {
        whenever(topicDao.observeTopicById("topic-1")).thenReturn(flowOf(testTopic))

        repository.getTopicById("topic-1").test {
            assertThat(awaitItem()).isEqualTo(testTopic)
            awaitComplete()
        }
    }

    @Test
    fun `getTopicByIdSync returns topic from dao`() = runTest {
        whenever(topicDao.getTopicById("topic-1")).thenReturn(testTopic)

        val result = repository.getTopicByIdSync("topic-1")

        assertThat(result).isEqualTo(testTopic)
    }

    @Test
    fun `getTopicByIdSync returns null for non-existent topic`() = runTest {
        whenever(topicDao.getTopicById("non-existent")).thenReturn(null)

        val result = repository.getTopicByIdSync("non-existent")

        assertThat(result).isNull()
    }

    // =====================
    // CREATE/UPDATE TESTS
    // =====================

    @Test
    fun `insertTopic calls dao insert`() = runTest {
        repository.insertTopic(testTopic)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).insertTopic(testTopic)
    }

    @Test
    fun `updateTopic calls dao update`() = runTest {
        val updatedTopic = testTopic.copy(title = "Updated Title")

        repository.updateTopic(updatedTopic)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).updateTopic(updatedTopic)
    }

    // =====================
    // SEARCH TESTS
    // =====================

    @Test
    fun `searchTopics uses FTS search`() = runTest {
        val topics = listOf(testTopic)
        whenever(topicDao.searchTopicsFts("test")).thenReturn(flowOf(topics))

        repository.searchTopics("test").test {
            assertThat(awaitItem()).isEqualTo(topics)
            awaitComplete()
        }
    }

    @Test
    fun `searchTopics returns empty list when no matches`() = runTest {
        whenever(topicDao.searchTopicsFts("nonexistent")).thenReturn(flowOf(emptyList()))

        repository.searchTopics("nonexistent").test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    // =====================
    // MULTIPLE TOPICS TESTS
    // =====================

    @Test
    fun `getAllTopics returns multiple topics`() = runTest {
        val topic2 = Topic(
            id = "topic-2",
            title = "Second Topic",
            summary = "Second Summary",
            createdAt = testTimestamp,
            updatedAt = testTimestamp
        )
        val topics = listOf(testTopic, topic2)
        whenever(topicDao.getAllTopics()).thenReturn(flowOf(topics))

        repository.getAllTopics().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].id).isEqualTo("topic-1")
            assertThat(result[1].id).isEqualTo("topic-2")
            awaitComplete()
        }
    }

    @Test
    fun `getAllTopics handles empty list`() = runTest {
        whenever(topicDao.getAllTopics()).thenReturn(flowOf(emptyList()))

        repository.getAllTopics().test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    // =====================
    // TOPIC POSTURE TESTS
    // =====================

    @Test
    fun `insertTopic preserves posture`() = runTest {
        val topicWithPosture = testTopic.copy(posture = Topic.Posture.SKEPTICAL)

        repository.insertTopic(topicWithPosture)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).insertTopic(argThat { topic ->
            topic.posture == Topic.Posture.SKEPTICAL
        })
    }

    @Test
    fun `updateTopic preserves posture`() = runTest {
        val topicWithPosture = testTopic.copy(posture = Topic.Posture.ACADEMIC_COMPARATIVE)

        repository.updateTopic(topicWithPosture)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).updateTopic(argThat { topic ->
            topic.posture == Topic.Posture.ACADEMIC_COMPARATIVE
        })
    }

    // =====================
    // TOPIC TAGS TESTS
    // =====================

    @Test
    fun `insertTopic preserves tags`() = runTest {
        val topicWithTags = testTopic.copy(tags = listOf("tag-1", "tag-2"))

        repository.insertTopic(topicWithTags)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).insertTopic(argThat { topic ->
            topic.tags == listOf("tag-1", "tag-2")
        })
    }

    @Test
    fun `updateTopic preserves tags`() = runTest {
        val topicWithTags = testTopic.copy(tags = listOf("tag-1", "tag-2", "tag-3"))

        repository.updateTopic(topicWithTags)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).updateTopic(argThat { topic ->
            topic.tags.size == 3
        })
    }
}
