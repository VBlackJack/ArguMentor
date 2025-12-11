package com.argumentor.app.ui.screens.statistics

import app.cash.turbine.test
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.repository.Statistics
import com.argumentor.app.data.repository.StatisticsRepository
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var statisticsRepository: StatisticsRepository
    private lateinit var viewModel: StatisticsViewModel

    private val testStatistics = Statistics(
        totalTopics = 10,
        totalClaims = 50,
        totalRebuttals = 30,
        totalQuestions = 20,
        totalSources = 15,
        totalEvidence = 25,
        claimsByStance = mapOf(
            Claim.Stance.PRO to 20,
            Claim.Stance.CON to 15,
            Claim.Stance.NEUTRAL to 15
        ),
        topicsByPosture = mapOf(
            Topic.Posture.NEUTRAL_CRITICAL to 5,
            Topic.Posture.SKEPTICAL to 3,
            Topic.Posture.ACADEMIC_COMPARATIVE to 2
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        statisticsRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default Statistics`() = runTest {
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(Statistics()))

        viewModel = StatisticsViewModel(statisticsRepository)

        assertThat(viewModel.statistics.value).isEqualTo(Statistics())
    }

    @Test
    fun `statistics emits values from repository`() = runTest {
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(testStatistics))

        viewModel = StatisticsViewModel(statisticsRepository)

        viewModel.statistics.test {
            // Initial value
            assertThat(awaitItem()).isEqualTo(Statistics())

            testDispatcher.scheduler.advanceUntilIdle()

            val stats = awaitItem()
            assertThat(stats.totalTopics).isEqualTo(10)
            assertThat(stats.totalClaims).isEqualTo(50)
            assertThat(stats.totalRebuttals).isEqualTo(30)
            assertThat(stats.totalQuestions).isEqualTo(20)
            assertThat(stats.totalSources).isEqualTo(15)
            assertThat(stats.totalEvidence).isEqualTo(25)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `statistics contains claimsByStance breakdown`() = runTest {
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(testStatistics))

        viewModel = StatisticsViewModel(statisticsRepository)

        viewModel.statistics.test {
            skipItems(1) // Skip initial
            testDispatcher.scheduler.advanceUntilIdle()

            val stats = awaitItem()
            assertThat(stats.claimsByStance).containsEntry(Claim.Stance.PRO, 20)
            assertThat(stats.claimsByStance).containsEntry(Claim.Stance.CON, 15)
            assertThat(stats.claimsByStance).containsEntry(Claim.Stance.NEUTRAL, 15)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `statistics contains topicsByPosture breakdown`() = runTest {
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(testStatistics))

        viewModel = StatisticsViewModel(statisticsRepository)

        viewModel.statistics.test {
            skipItems(1) // Skip initial
            testDispatcher.scheduler.advanceUntilIdle()

            val stats = awaitItem()
            assertThat(stats.topicsByPosture).containsEntry(Topic.Posture.NEUTRAL_CRITICAL, 5)
            assertThat(stats.topicsByPosture).containsEntry(Topic.Posture.SKEPTICAL, 3)
            assertThat(stats.topicsByPosture).containsEntry(Topic.Posture.ACADEMIC_COMPARATIVE, 2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `statistics handles empty data`() = runTest {
        val emptyStatistics = Statistics(
            totalTopics = 0,
            totalClaims = 0,
            totalRebuttals = 0,
            totalQuestions = 0,
            totalSources = 0,
            totalEvidence = 0,
            claimsByStance = emptyMap(),
            topicsByPosture = emptyMap()
        )
        whenever(statisticsRepository.getStatistics()).thenReturn(flowOf(emptyStatistics))

        viewModel = StatisticsViewModel(statisticsRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val stats = viewModel.statistics.value
        assertThat(stats.totalTopics).isEqualTo(0)
        assertThat(stats.totalClaims).isEqualTo(0)
        assertThat(stats.claimsByStance).isEmpty()
        assertThat(stats.topicsByPosture).isEmpty()
    }
}
