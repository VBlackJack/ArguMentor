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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TopicRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var database: ArguMentorDatabase
    private lateinit var topicDao: TopicDao
    private lateinit var claimDao: ClaimDao
    private lateinit var questionDao: QuestionDao
    private lateinit var repository: TopicRepository

    private val testTopic = Topic(
        id = "topic-1",
        title = "Test Topic",
        summary = "Test Summary",
        tags = listOf("tag1", "tag2")
    )

    private val testClaim = Claim(
        id = "claim-1",
        text = "Test Claim",
        topics = listOf("topic-1")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        database = mock()
        topicDao = mock()
        claimDao = mock()
        questionDao = mock()

        repository = TopicRepository(
            database = database,
            topicDao = topicDao,
            claimDao = claimDao,
            questionDao = questionDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
    fun `getTopicById returns null for non-existent topic`() = runTest {
        whenever(topicDao.observeTopicById("non-existent")).thenReturn(flowOf(null))

        repository.getTopicById("non-existent").test {
            assertThat(awaitItem()).isNull()
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
    fun `insertTopic calls dao insert`() = runTest {
        repository.insertTopic(testTopic)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).insertTopic(testTopic)
    }

    @Test
    fun `updateTopic calls dao update`() = runTest {
        repository.updateTopic(testTopic)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicDao).updateTopic(testTopic)
    }

    @Test
    fun `deleteTopic deletes topic and related entities`() = runTest {
        whenever(claimDao.getClaimsByTopicId("topic-1")).thenReturn(listOf(testClaim))

        // Mock withTransaction to execute the block directly
        whenever(database.withTransaction<Unit>(any())).thenAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val block = invocation.arguments[0] as suspend () -> Unit
            kotlinx.coroutines.runBlocking { block() }
        }

        repository.deleteTopic(testTopic)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimDao).getClaimsByTopicId("topic-1")
        verify(questionDao).deleteQuestionsByTargetId("claim-1")
        verify(claimDao).deleteClaimsByTopicId("topic-1")
        verify(questionDao).deleteQuestionsByTargetId("topic-1")
        verify(topicDao).deleteTopicById("topic-1")
    }

    @Test
    fun `deleteTopicById deletes topic with no claims`() = runTest {
        whenever(claimDao.getClaimsByTopicId("topic-1")).thenReturn(emptyList())

        whenever(database.withTransaction<Unit>(any())).thenAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val block = invocation.arguments[0] as suspend () -> Unit
            kotlinx.coroutines.runBlocking { block() }
        }

        repository.deleteTopicById("topic-1")

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimDao).deleteClaimsByTopicId("topic-1")
        verify(questionDao).deleteQuestionsByTargetId("topic-1")
        verify(topicDao).deleteTopicById("topic-1")
        verify(questionDao, never()).deleteQuestionsByTargetId("claim-1")
    }

    @Test
    fun `searchTopics uses FTS search when valid query`() = runTest {
        val topics = listOf(testTopic)
        whenever(topicDao.searchTopicsFts("test")).thenReturn(flowOf(topics))

        repository.searchTopics("test").test {
            assertThat(awaitItem()).isEqualTo(topics)
            awaitComplete()
        }
    }
}
