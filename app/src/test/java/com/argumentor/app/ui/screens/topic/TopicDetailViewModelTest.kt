package com.argumentor.app.ui.screens.topic

import app.cash.turbine.test
import com.argumentor.app.R
import com.argumentor.app.data.export.MarkdownExporter
import com.argumentor.app.data.export.PdfExporter
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import com.argumentor.app.util.ResourceProvider
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TopicDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var topicRepository: TopicRepository
    private lateinit var claimRepository: ClaimRepository
    private lateinit var rebuttalRepository: RebuttalRepository
    private lateinit var evidenceRepository: EvidenceRepository
    private lateinit var questionRepository: QuestionRepository
    private lateinit var sourceRepository: SourceRepository
    private lateinit var pdfExporter: PdfExporter
    private lateinit var markdownExporter: MarkdownExporter
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var viewModel: TopicDetailViewModel

    private val testTopic = Topic(
        id = "topic-1",
        title = "Test Topic",
        summary = "Test Summary"
    )

    private val testClaim1 = Claim(
        id = "claim-1",
        text = "Test Claim 1",
        topics = listOf("topic-1"),
        stance = Claim.Stance.PRO
    )

    private val testClaim2 = Claim(
        id = "claim-2",
        text = "Test Claim 2",
        topics = listOf("topic-1", "topic-2"),
        stance = Claim.Stance.CON
    )

    private val testQuestion = Question(
        id = "question-1",
        targetId = "topic-1",
        text = "Test Question?",
        type = Question.Type.SOCRATIC
    )

    private val testEvidence = Evidence(
        id = "evidence-1",
        claimId = "claim-1",
        content = "Test Evidence",
        type = Evidence.Type.STUDY
    )

    private val testSource = Source(
        id = "source-1",
        title = "Test Source"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        topicRepository = mock()
        claimRepository = mock()
        rebuttalRepository = mock()
        evidenceRepository = mock()
        questionRepository = mock()
        sourceRepository = mock()
        pdfExporter = mock()
        markdownExporter = mock()
        resourceProvider = mock()

        whenever(resourceProvider.getString(R.string.error_export_pdf)).thenReturn("PDF export error")
        whenever(resourceProvider.getString(R.string.error_export_markdown)).thenReturn("Markdown export error")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TopicDetailViewModel {
        return TopicDetailViewModel(
            topicRepository = topicRepository,
            claimRepository = claimRepository,
            rebuttalRepository = rebuttalRepository,
            evidenceRepository = evidenceRepository,
            questionRepository = questionRepository,
            sourceRepository = sourceRepository,
            pdfExporter = pdfExporter,
            markdownExporter = markdownExporter,
            resourceProvider = resourceProvider
        )
    }

    private fun setupDefaultMocks() {
        whenever(topicRepository.getTopicById("topic-1")).thenReturn(flowOf(testTopic))
        whenever(claimRepository.getAllClaims()).thenReturn(flowOf(listOf(testClaim1, testClaim2)))
        whenever(sourceRepository.getAllSources()).thenReturn(flowOf(listOf(testSource)))
        whenever(questionRepository.getQuestionsByTargetId("topic-1")).thenReturn(flowOf(listOf(testQuestion)))
        whenever(questionRepository.getQuestionsByTargetId("claim-1")).thenReturn(flowOf(emptyList()))
        whenever(questionRepository.getQuestionsByTargetId("claim-2")).thenReturn(flowOf(emptyList()))
        whenever(evidenceRepository.getAllEvidences()).thenReturn(flowOf(listOf(testEvidence)))
    }

    @Test
    fun `initial state has null topic`() {
        setupDefaultMocks()
        viewModel = createViewModel()

        assertThat(viewModel.topic.value).isNull()
    }

    @Test
    fun `loadTopic loads topic data correctly`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.topic.test {
            assertThat(awaitItem()).isNull()

            viewModel.loadTopic("topic-1")
            testDispatcher.scheduler.advanceUntilIdle()

            val topic = awaitItem()
            assertThat(topic).isEqualTo(testTopic)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTopic filters claims for the specific topic`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.claims.test {
            assertThat(awaitItem()).isEmpty()

            viewModel.loadTopic("topic-1")
            testDispatcher.scheduler.advanceUntilIdle()

            val claims = awaitItem()
            assertThat(claims).hasSize(2)
            assertThat(claims).containsExactly(testClaim1, testClaim2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTopic loads questions correctly`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.questions.test {
            assertThat(awaitItem()).isEmpty()

            viewModel.loadTopic("topic-1")
            testDispatcher.scheduler.advanceUntilIdle()

            val questions = awaitItem()
            assertThat(questions).hasSize(1)
            assertThat(questions.first()).isEqualTo(testQuestion)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTopic loads sources correctly`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.sources.test {
            assertThat(awaitItem()).isEmpty()

            viewModel.loadTopic("topic-1")
            testDispatcher.scheduler.advanceUntilIdle()

            val sources = awaitItem()
            assertThat(sources).hasSize(1)
            assertThat(sources.first()).isEqualTo(testSource)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTopic groups evidences by claimId`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.evidencesByClaimId.test {
            assertThat(awaitItem()).isEmpty()

            viewModel.loadTopic("topic-1")
            testDispatcher.scheduler.advanceUntilIdle()

            val evidencesMap = awaitItem()
            assertThat(evidencesMap).containsKey("claim-1")
            assertThat(evidencesMap["claim-1"]).containsExactly(testEvidence)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTabSelected updates selectedTab`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        assertThat(viewModel.selectedTab.value).isEqualTo(0)

        viewModel.onTabSelected(2)

        assertThat(viewModel.selectedTab.value).isEqualTo(2)
    }

    @Test
    fun `deleteTopic calls repository and invokes callback`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        var callbackCalled = false
        viewModel.deleteTopic { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicRepository).deleteTopic(testTopic)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `deleteClaim calls repository and invokes callback`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteClaim(testClaim1) { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimRepository).deleteClaim(testClaim1)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreClaim calls repository insert`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.restoreClaim(testClaim1)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimRepository).insertClaim(testClaim1)
    }

    @Test
    fun `deleteQuestion calls repository and invokes callback`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteQuestion(testQuestion) { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        verify(questionRepository).deleteQuestion(testQuestion)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreQuestion calls repository insert`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.restoreQuestion(testQuestion)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(questionRepository).insertQuestion(testQuestion)
    }

    @Test
    fun `deleteEvidence calls repository and invokes callback`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteEvidence(testEvidence) { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceRepository).deleteEvidence(testEvidence)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreEvidence calls repository insert`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.restoreEvidence(testEvidence)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceRepository).insertEvidence(testEvidence)
    }

    @Test
    fun `deleteSource calls repository and invokes callback`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteSource(testSource) { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceRepository).deleteSource(testSource)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreSource calls repository insert`() = runTest {
        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.restoreSource(testSource)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceRepository).insertSource(testSource)
    }

    @Test
    fun `getClaimRebuttals returns flow from repository`() = runTest {
        val rebuttals = listOf(
            Rebuttal(id = "rebuttal-1", claimId = "claim-1", text = "Test Rebuttal")
        )
        whenever(rebuttalRepository.getRebuttalsByClaimId("claim-1")).thenReturn(flowOf(rebuttals))

        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.getClaimRebuttals("claim-1").test {
            val result = awaitItem()
            assertThat(result).isEqualTo(rebuttals)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getClaimEvidences returns flow from repository`() = runTest {
        val evidences = listOf(testEvidence)
        whenever(evidenceRepository.getEvidencesByClaimId("claim-1")).thenReturn(flowOf(evidences))

        setupDefaultMocks()
        viewModel = createViewModel()

        viewModel.getClaimEvidences("claim-1").test {
            val result = awaitItem()
            assertThat(result).isEqualTo(evidences)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loading new topic cancels previous load`() = runTest {
        val topic2 = Topic(id = "topic-2", title = "Topic 2", summary = "Summary 2")
        whenever(topicRepository.getTopicById("topic-2")).thenReturn(flowOf(topic2))
        whenever(questionRepository.getQuestionsByTargetId("topic-2")).thenReturn(flowOf(emptyList()))

        setupDefaultMocks()
        viewModel = createViewModel()

        // Start loading first topic
        viewModel.loadTopic("topic-1")

        // Immediately load second topic (should cancel first)
        viewModel.loadTopic("topic-2")

        testDispatcher.scheduler.advanceUntilIdle()

        // Should end up with topic-2 loaded
        assertThat(viewModel.topic.value?.id).isEqualTo("topic-2")
    }
}
