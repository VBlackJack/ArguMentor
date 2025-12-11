package com.argumentor.app.ui.screens.debate

import app.cash.turbine.test
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class DebateModeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var topicRepository: TopicRepository
    private lateinit var claimRepository: ClaimRepository
    private lateinit var rebuttalRepository: RebuttalRepository
    private lateinit var evidenceRepository: EvidenceRepository
    private lateinit var questionRepository: QuestionRepository
    private lateinit var fallacyRepository: FallacyRepository
    private lateinit var viewModel: DebateModeViewModel

    private val testTopic = Topic(
        id = "topic-1",
        title = "Test Topic",
        summary = "Test summary"
    )

    private val testClaim1 = Claim(
        id = "claim-1",
        text = "Test claim 1",
        stance = Claim.Stance.PRO,
        topics = listOf("topic-1")
    )

    private val testClaim2 = Claim(
        id = "claim-2",
        text = "Test claim 2",
        stance = Claim.Stance.CON,
        topics = listOf("topic-1")
    )

    private val testRebuttal = Rebuttal(
        id = "rebuttal-1",
        text = "Test rebuttal",
        claimId = "claim-1"
    )

    private val testEvidence = Evidence(
        id = "evidence-1",
        content = "Test evidence",
        claimId = "claim-1",
        type = Evidence.EvidenceType.STUDY,
        quality = Evidence.Quality.HIGH
    )

    private val testQuestion = Question(
        id = "question-1",
        text = "Test question?",
        targetId = "topic-1",
        kind = Question.QuestionKind.SOCRATIC
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        topicRepository = mock()
        claimRepository = mock()
        rebuttalRepository = mock()
        evidenceRepository = mock()
        questionRepository = mock()
        fallacyRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DebateModeViewModel {
        return DebateModeViewModel(
            topicRepository = topicRepository,
            claimRepository = claimRepository,
            rebuttalRepository = rebuttalRepository,
            evidenceRepository = evidenceRepository,
            questionRepository = questionRepository,
            fallacyRepository = fallacyRepository
        )
    }

    @Test
    fun `initial state has null topic and empty cards`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        assertThat(viewModel.topic.value).isNull()
        assertThat(viewModel.debateCards.value).isEmpty()
        assertThat(viewModel.currentCardIndex.value).isEqualTo(0)
        assertThat(viewModel.isCardFlipped.value).isFalse()
    }

    @Test
    fun `initial gamification state is zero`() = runTest {
        setupMocksForEmptyData()
        viewModel = createViewModel()

        assertThat(viewModel.cardsReviewed.value).isEmpty()
        assertThat(viewModel.sessionScore.value).isEqualTo(0)
        assertThat(viewModel.streak.value).isEqualTo(0)
    }

    @Test
    fun `loadTopic loads topic and creates debate cards`() = runTest {
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
    fun `loadTopic creates cards for each claim`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()

        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.debateCards.test {
            val cards = awaitItem()
            assertThat(cards).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nextCard advances to next card`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.currentCardIndex.value).isEqualTo(0)

        viewModel.nextCard()
        assertThat(viewModel.currentCardIndex.value).isEqualTo(1)
    }

    @Test
    fun `nextCard does not exceed last card`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextCard() // Go to card 1
        viewModel.nextCard() // Try to go beyond

        assertThat(viewModel.currentCardIndex.value).isEqualTo(1) // Should stay at last card
    }

    @Test
    fun `previousCard goes to previous card`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextCard() // Go to card 1
        viewModel.previousCard() // Go back to card 0

        assertThat(viewModel.currentCardIndex.value).isEqualTo(0)
    }

    @Test
    fun `previousCard does not go below zero`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.previousCard() // Try to go below 0

        assertThat(viewModel.currentCardIndex.value).isEqualTo(0)
    }

    @Test
    fun `goToCard navigates to specific card`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.goToCard(1)

        assertThat(viewModel.currentCardIndex.value).isEqualTo(1)
    }

    @Test
    fun `goToCard ignores invalid index`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.goToCard(100) // Invalid index

        assertThat(viewModel.currentCardIndex.value).isEqualTo(0) // Should stay at 0
    }

    @Test
    fun `flipCard toggles card flip state`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isCardFlipped.value).isFalse()

        viewModel.flipCard()
        assertThat(viewModel.isCardFlipped.value).isTrue()

        viewModel.flipCard()
        assertThat(viewModel.isCardFlipped.value).isFalse()
    }

    @Test
    fun `flipCard to answer side marks card as reviewed`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.cardsReviewed.value).isEmpty()

        viewModel.flipCard() // Flip to answer side

        assertThat(viewModel.cardsReviewed.value).isNotEmpty()
    }

    @Test
    fun `flipCard to answer side increases score`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.sessionScore.value).isEqualTo(0)

        viewModel.flipCard() // Flip to answer side

        assertThat(viewModel.sessionScore.value).isGreaterThan(0)
    }

    @Test
    fun `flipCard to answer side increases streak`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.streak.value).isEqualTo(0)

        viewModel.flipCard() // Flip to answer side

        assertThat(viewModel.streak.value).isEqualTo(1)
    }

    @Test
    fun `flipping same card twice does not double count score`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.flipCard() // First flip - scores
        val scoreAfterFirstFlip = viewModel.sessionScore.value

        viewModel.flipCard() // Flip back
        viewModel.flipCard() // Flip to answer again - should not score

        assertThat(viewModel.sessionScore.value).isEqualTo(scoreAfterFirstFlip)
    }

    @Test
    fun `markCardDifficult resets streak`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.flipCard() // Build streak
        assertThat(viewModel.streak.value).isEqualTo(1)

        viewModel.markCardDifficult()
        assertThat(viewModel.streak.value).isEqualTo(0)
    }

    @Test
    fun `resetProgress resets all gamification state`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Build up some progress
        viewModel.flipCard()
        viewModel.nextCard()

        assertThat(viewModel.currentCardIndex.value).isNotEqualTo(0)
        assertThat(viewModel.sessionScore.value).isGreaterThan(0)
        assertThat(viewModel.cardsReviewed.value).isNotEmpty()

        viewModel.resetProgress()

        assertThat(viewModel.currentCardIndex.value).isEqualTo(0)
        assertThat(viewModel.isCardFlipped.value).isFalse()
        assertThat(viewModel.sessionScore.value).isEqualTo(0)
        assertThat(viewModel.streak.value).isEqualTo(0)
        assertThat(viewModel.cardsReviewed.value).isEmpty()
    }

    @Test
    fun `navigating to new card resets flip state`() = runTest {
        setupMocksForTestData()
        viewModel = createViewModel()
        viewModel.loadTopic("topic-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.flipCard() // Flip card
        assertThat(viewModel.isCardFlipped.value).isTrue()

        viewModel.nextCard() // Navigate to next card
        assertThat(viewModel.isCardFlipped.value).isFalse() // Should reset
    }

    private fun setupMocksForEmptyData() {
        whenever(topicRepository.getTopicById(any())).thenReturn(flowOf(null))
        whenever(claimRepository.getAllClaims()).thenReturn(flowOf(emptyList()))
        whenever(rebuttalRepository.getAllRebuttals()).thenReturn(flowOf(emptyList()))
        whenever(evidenceRepository.getAllEvidences()).thenReturn(flowOf(emptyList()))
        whenever(questionRepository.getQuestionsByTargetId(any())).thenReturn(flowOf(emptyList()))
        runBlocking {
            whenever(fallacyRepository.getFallaciesByIds(any())).thenReturn(emptyList())
        }
    }

    private fun setupMocksForTestData() {
        whenever(topicRepository.getTopicById("topic-1")).thenReturn(flowOf(testTopic))
        whenever(claimRepository.getAllClaims()).thenReturn(flowOf(listOf(testClaim1, testClaim2)))
        whenever(rebuttalRepository.getAllRebuttals()).thenReturn(flowOf(listOf(testRebuttal)))
        whenever(evidenceRepository.getAllEvidences()).thenReturn(flowOf(listOf(testEvidence)))
        whenever(questionRepository.getQuestionsByTargetId("topic-1")).thenReturn(flowOf(listOf(testQuestion)))
        runBlocking {
            whenever(fallacyRepository.getFallaciesByIds(any())).thenReturn(emptyList())
        }
    }
}
