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
import org.mockito.kotlin.*
import java.io.ByteArrayOutputStream

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
        summary = "Test summary",
        tags = listOf("tag1")
    )

    private val testClaim1 = Claim(
        id = "claim-1",
        text = "Test claim 1",
        stance = Claim.Stance.PRO,
        strength = Claim.Strength.HIGH,
        topics = listOf("topic-1")
    )

    private val testClaim2 = Claim(
        id = "claim-2",
        text = "Test claim 2",
        stance = Claim.Stance.CON,
        strength = Claim.Strength.MEDIUM,
        topics = listOf("topic-1")
    )

    private val testQuestion = Question(
        id = "question-1",
        text = "Test question?",
        targetId = "topic-1",
        kind = Question.QuestionKind.SOCRATIC
    )

    private val testSource = Source(
        id = "source-1",
        title = "Test Source",
        url = "https://example.com"
    )

    private val testEvidence = Evidence(
        id = "evidence-1",
        content = "Test evidence content",
        claimId = "claim-1",
        type = Evidence.EvidenceType.STUDY,
        quality = Evidence.Quality.HIGH
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

        whenever(resourceProvider.getString(R.string.error_export_pdf)).thenReturn("Export PDF failed")
        whenever(resourceProvider.getString(R.string.error_export_markdown)).thenReturn("Export Markdown failed")
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

    @Test
    fun `initial state has null topic and empty lists`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        assertThat(viewModel.topic.value).isNull()
        assertThat(viewModel.claims.value).isEmpty()
        assertThat(viewModel.questions.value).isEmpty()
        assertThat(viewModel.sources.value).isEmpty()
    }

    @Test
    fun `loadTopic loads topic and related data`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()

        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.topic.test {
            val topic = awaitItem()
            assertThat(topic).isEqualTo(testTopic)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTopic filters claims by topic ID`() = runTest {
        val claimForOtherTopic = Claim(
            id = "claim-other",
            text = "Other topic claim",
            topics = listOf("other-topic")
        )

        whenever(topicRepository.getTopicById("topic-1")).thenReturn(flowOf(testTopic))
        whenever(claimRepository.getAllClaims()).thenReturn(flowOf(listOf(testClaim1, testClaim2, claimForOtherTopic)))
        whenever(sourceRepository.getAllSources()).thenReturn(flowOf(emptyList()))
        whenever(questionRepository.getQuestionsByTargetId(any())).thenReturn(flowOf(emptyList()))
        whenever(evidenceRepository.getAllEvidences()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.claims.test {
            val claims = awaitItem()
            assertThat(claims).hasSize(2)
            assertThat(claims).containsExactly(testClaim1, testClaim2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTabSelected updates selectedTab state`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        assertThat(viewModel.selectedTab.value).isEqualTo(0)

        viewModel.onTabSelected(1)
        assertThat(viewModel.selectedTab.value).isEqualTo(1)

        viewModel.onTabSelected(2)
        assertThat(viewModel.selectedTab.value).isEqualTo(2)
    }

    @Test
    fun `deleteTopic calls repository and invokes callback`() = runTest {
        setupMocksForTestData()
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
        setupMocksForEmptyData()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteClaim(testClaim1) { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimRepository).deleteClaim(testClaim1)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreClaim calls repository insert`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        viewModel.restoreClaim(testClaim1)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(claimRepository).insertClaim(testClaim1)
    }

    @Test
    fun `deleteQuestion calls repository and invokes callback`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteQuestion(testQuestion) { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        verify(questionRepository).deleteQuestion(testQuestion)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreQuestion calls repository insert`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        viewModel.restoreQuestion(testQuestion)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(questionRepository).insertQuestion(testQuestion)
    }

    @Test
    fun `deleteEvidence calls repository and invokes callback`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteEvidence(testEvidence) { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceRepository).deleteEvidence(testEvidence)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreEvidence calls repository insert`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        viewModel.restoreEvidence(testEvidence)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(evidenceRepository).insertEvidence(testEvidence)
    }

    @Test
    fun `deleteSource calls repository and invokes callback`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteSource(testSource) { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceRepository).deleteSource(testSource)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreSource calls repository insert`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        viewModel.restoreSource(testSource)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceRepository).insertSource(testSource)
    }

    @Test
    fun `exportTopicToPdf calls exporter and returns success`() = runTest {
        setupMocksForTestData()
        whenever(rebuttalRepository.getAllRebuttals()).thenReturn(flowOf(emptyList()))
        whenever(pdfExporter.exportTopicToPdf(any(), any(), any(), any())).thenReturn(Result.success(Unit))

        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val outputStream = ByteArrayOutputStream()
        var success = false
        var errorMessage: String? = "initial"

        viewModel.exportTopicToPdf("topic-1", outputStream) { s, e ->
            success = s
            errorMessage = e
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(success).isTrue()
        assertThat(errorMessage).isNull()
    }

    @Test
    fun `exportTopicToPdf returns error on failure`() = runTest {
        setupMocksForTestData()
        whenever(rebuttalRepository.getAllRebuttals()).thenReturn(flowOf(emptyList()))
        whenever(pdfExporter.exportTopicToPdf(any(), any(), any(), any()))
            .thenReturn(Result.failure(RuntimeException("PDF error")))

        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val outputStream = ByteArrayOutputStream()
        var success = true
        var errorMessage: String? = null

        viewModel.exportTopicToPdf("topic-1", outputStream) { s, e ->
            success = s
            errorMessage = e
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(success).isFalse()
        assertThat(errorMessage).isEqualTo("PDF error")
    }

    @Test
    fun `evidencesByClaimId groups evidences correctly`() = runTest {
        val evidence1 = testEvidence.copy(id = "ev-1", claimId = "claim-1")
        val evidence2 = testEvidence.copy(id = "ev-2", claimId = "claim-1")
        val evidence3 = testEvidence.copy(id = "ev-3", claimId = "claim-2")

        whenever(topicRepository.getTopicById("topic-1")).thenReturn(flowOf(testTopic))
        whenever(claimRepository.getAllClaims()).thenReturn(flowOf(listOf(testClaim1, testClaim2)))
        whenever(sourceRepository.getAllSources()).thenReturn(flowOf(emptyList()))
        whenever(questionRepository.getQuestionsByTargetId(any())).thenReturn(flowOf(emptyList()))
        whenever(evidenceRepository.getAllEvidences()).thenReturn(flowOf(listOf(evidence1, evidence2, evidence3)))

        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.evidencesByClaimId.test {
            val evidencesMap = awaitItem()
            assertThat(evidencesMap["claim-1"]).hasSize(2)
            assertThat(evidencesMap["claim-2"]).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun setupMocksForEmptyData() {
        whenever(topicRepository.getTopicById(any())).thenReturn(flowOf(null))
        whenever(claimRepository.getAllClaims()).thenReturn(flowOf(emptyList()))
        whenever(sourceRepository.getAllSources()).thenReturn(flowOf(emptyList()))
        whenever(questionRepository.getQuestionsByTargetId(any())).thenReturn(flowOf(emptyList()))
        whenever(evidenceRepository.getAllEvidences()).thenReturn(flowOf(emptyList()))
    }

    private fun setupMocksForTestData() {
        whenever(topicRepository.getTopicById("topic-1")).thenReturn(flowOf(testTopic))
        whenever(claimRepository.getAllClaims()).thenReturn(flowOf(listOf(testClaim1, testClaim2)))
        whenever(sourceRepository.getAllSources()).thenReturn(flowOf(listOf(testSource)))
        whenever(questionRepository.getQuestionsByTargetId("topic-1")).thenReturn(flowOf(listOf(testQuestion)))
        whenever(questionRepository.getQuestionsByTargetId("claim-1")).thenReturn(flowOf(emptyList()))
        whenever(questionRepository.getQuestionsByTargetId("claim-2")).thenReturn(flowOf(emptyList()))
        whenever(evidenceRepository.getAllEvidences()).thenReturn(flowOf(listOf(testEvidence)))
    }
}
