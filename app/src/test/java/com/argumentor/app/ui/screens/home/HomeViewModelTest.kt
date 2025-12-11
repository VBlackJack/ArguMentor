package com.argumentor.app.ui.screens.home

import app.cash.turbine.test
import com.argumentor.app.R
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.repository.TopicRepository
import com.argumentor.app.ui.common.UiState
import com.argumentor.app.util.ResourceProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var topicRepository: TopicRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var viewModel: HomeViewModel

    private val testTopic1 = Topic(
        id = "topic-1",
        title = "Test Topic 1",
        summary = "Summary 1",
        tags = listOf("tag1", "tag2")
    )

    private val testTopic2 = Topic(
        id = "topic-2",
        title = "Test Topic 2",
        summary = "Summary 2",
        tags = listOf("tag2", "tag3")
    )

    private val testTopic3 = Topic(
        id = "topic-3",
        title = "Another Subject",
        summary = "Different summary",
        tags = listOf("tag1")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        topicRepository = mock()
        resourceProvider = mock()
        settingsDataStore = mock()

        whenever(resourceProvider.getString(R.string.error_unknown)).thenReturn("Unknown error")
        whenever(settingsDataStore.demoTopicId).thenReturn(flowOf(null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            topicRepository = topicRepository,
            resourceProvider = resourceProvider,
            settingsDataStore = settingsDataStore
        )
    }

    @Test
    fun `initial state is Loading`() = runTest {
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(UiState.Loading)
    }

    @Test
    fun `uiState emits Success when topics are loaded`() = runTest {
        val topics = listOf(testTopic1, testTopic2)
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(topics))

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(UiState.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem()
            assertThat(successState).isInstanceOf(UiState.Success::class.java)
            assertThat((successState as UiState.Success).data).containsExactly(testTopic1, testTopic2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Empty when no topics exist`() = runTest {
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(UiState.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            assertThat(awaitItem()).isEqualTo(UiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search filters topics by title`() = runTest {
        val topics = listOf(testTopic1, testTopic2, testTopic3)
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(topics))

        viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            testDispatcher.scheduler.advanceUntilIdle()
            skipItems(1) // Skip initial Success

            viewModel.onSearchQueryChange("Test")

            // Advance past debounce (300ms)
            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            val filteredState = awaitItem()
            assertThat(filteredState).isInstanceOf(UiState.Success::class.java)
            val filteredTopics = (filteredState as UiState.Success).data
            assertThat(filteredTopics).hasSize(2)
            assertThat(filteredTopics).containsExactly(testTopic1, testTopic2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search filters topics by summary`() = runTest {
        val topics = listOf(testTopic1, testTopic2, testTopic3)
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(topics))

        viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            testDispatcher.scheduler.advanceUntilIdle()
            skipItems(1)

            viewModel.onSearchQueryChange("Different")

            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            val filteredState = awaitItem()
            assertThat(filteredState).isInstanceOf(UiState.Success::class.java)
            val filteredTopics = (filteredState as UiState.Success).data
            assertThat(filteredTopics).hasSize(1)
            assertThat(filteredTopics.first()).isEqualTo(testTopic3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tag filter filters topics by selected tag`() = runTest {
        val topics = listOf(testTopic1, testTopic2, testTopic3)
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(topics))

        viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            testDispatcher.scheduler.advanceUntilIdle()
            skipItems(1)

            viewModel.onTagSelected("tag3")
            testDispatcher.scheduler.advanceUntilIdle()

            val filteredState = awaitItem()
            assertThat(filteredState).isInstanceOf(UiState.Success::class.java)
            val filteredTopics = (filteredState as UiState.Success).data
            assertThat(filteredTopics).hasSize(1)
            assertThat(filteredTopics.first()).isEqualTo(testTopic2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting same tag twice clears the filter`() = runTest {
        val topics = listOf(testTopic1, testTopic2)
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(topics))

        viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            testDispatcher.scheduler.advanceUntilIdle()
            skipItems(1)

            // Select tag
            viewModel.onTagSelected("tag1")
            testDispatcher.scheduler.advanceUntilIdle()

            val filteredState = awaitItem()
            assertThat((filteredState as UiState.Success).data).hasSize(1)

            // Select same tag again to clear
            viewModel.onTagSelected("tag1")
            testDispatcher.scheduler.advanceUntilIdle()

            val clearedState = awaitItem()
            assertThat((clearedState as UiState.Success).data).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `combined search and tag filter works correctly`() = runTest {
        val topics = listOf(testTopic1, testTopic2, testTopic3)
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(topics))

        viewModel = createViewModel()

        viewModel.uiState.test {
            skipItems(1)
            testDispatcher.scheduler.advanceUntilIdle()
            skipItems(1)

            // Set both filters
            viewModel.onTagSelected("tag1")
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            viewModel.onSearchQueryChange("Test")
            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            val combinedState = awaitItem()
            assertThat(combinedState).isInstanceOf(UiState.Success::class.java)
            val filteredTopics = (combinedState as UiState.Success).data
            // Only testTopic1 has tag1 AND contains "Test"
            assertThat(filteredTopics).hasSize(1)
            assertThat(filteredTopics.first()).isEqualTo(testTopic1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteTopic calls repository delete`() = runTest {
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(listOf(testTopic1)))

        viewModel = createViewModel()

        var callbackCalled = false
        viewModel.deleteTopic(testTopic1) { callbackCalled = true }

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicRepository).deleteTopic(testTopic1)
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `restoreTopic calls repository insert`() = runTest {
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()
        viewModel.restoreTopic(testTopic1)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(topicRepository).insertTopic(testTopic1)
    }

    @Test
    fun `refresh updates isRefreshing state`() = runTest {
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(listOf(testTopic1)))

        viewModel = createViewModel()

        viewModel.isRefreshing.test {
            assertThat(awaitItem()).isFalse()

            viewModel.refresh()

            assertThat(awaitItem()).isTrue()

            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery state updates correctly`() = runTest {
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()

        assertThat(viewModel.searchQuery.value).isEmpty()

        viewModel.onSearchQueryChange("test query")

        assertThat(viewModel.searchQuery.value).isEqualTo("test query")
    }

    @Test
    fun `selectedTag state updates correctly`() = runTest {
        whenever(topicRepository.getAllTopics()).thenReturn(flowOf(emptyList()))

        viewModel = createViewModel()

        assertThat(viewModel.selectedTag.value).isNull()

        viewModel.onTagSelected("tag1")

        assertThat(viewModel.selectedTag.value).isEqualTo("tag1")
    }

    @Test
    fun `demoTopicId emits value from settingsDataStore`() = runTest {
        val demoTopicFlow = MutableStateFlow<String?>("demo-topic-id")
        whenever(settingsDataStore.demoTopicId).thenReturn(demoTopicFlow)

        viewModel = createViewModel()

        viewModel.demoTopicId.test {
            // Initial value might be null, then updates
            testDispatcher.scheduler.advanceUntilIdle()
            val value = expectMostRecentItem()
            assertThat(value).isEqualTo("demo-topic-id")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
