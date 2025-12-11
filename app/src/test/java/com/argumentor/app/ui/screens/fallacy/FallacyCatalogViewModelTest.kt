package com.argumentor.app.ui.screens.fallacy

import android.app.Application
import app.cash.turbine.test
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.repository.FallacyRepository
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
class FallacyCatalogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var application: Application
    private lateinit var fallacyRepository: FallacyRepository
    private lateinit var viewModel: FallacyCatalogViewModel

    private val testFallacy1 = Fallacy(
        id = "ad_hominem",
        name = "Ad Hominem",
        description = "Attacking the person instead of the argument",
        example = "You can't trust his opinion because he's young",
        isCustom = false
    )

    private val testFallacy2 = Fallacy(
        id = "straw_man",
        name = "Straw Man",
        description = "Misrepresenting someone's argument",
        example = "He said we should reduce spending, so he wants to cut all public services",
        isCustom = false
    )

    private val customFallacy = Fallacy(
        id = "custom_1",
        name = "Custom Fallacy",
        description = "A custom fallacy",
        example = "Example of custom fallacy",
        isCustom = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        application = mock()
        fallacyRepository = mock()

        // Default mocks
        whenever(fallacyRepository.getAllFallacies()).thenReturn(flowOf(listOf(testFallacy1, testFallacy2)))
        whenever(fallacyRepository.getAllFallaciesSync()).thenReturn(listOf(testFallacy1, testFallacy2))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): FallacyCatalogViewModel {
        return FallacyCatalogViewModel(
            application = application,
            fallacyRepository = fallacyRepository
        )
    }

    @Test
    fun `initial searchQuery is empty`() = runTest {
        viewModel = createViewModel()

        assertThat(viewModel.searchQuery.value).isEmpty()
    }

    @Test
    fun `fallacies emits list from repository`() = runTest {
        viewModel = createViewModel()

        viewModel.fallacies.test {
            assertThat(awaitItem()).isEmpty() // Initial value
            testDispatcher.scheduler.advanceUntilIdle()

            val fallacies = awaitItem()
            assertThat(fallacies).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChange updates searchQuery`() = runTest {
        viewModel = createViewModel()

        viewModel.onSearchQueryChange("test")

        assertThat(viewModel.searchQuery.value).isEqualTo("test")
    }

    @Test
    fun `search filters fallacies through repository`() = runTest {
        whenever(fallacyRepository.searchFallacies("hominem")).thenReturn(flowOf(listOf(testFallacy1)))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("hominem")

        viewModel.fallacies.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val fallacies = expectMostRecentItem()
            assertThat(fallacies).hasSize(1)
            assertThat(fallacies.first().id).isEqualTo("ad_hominem")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearing search shows all fallacies`() = runTest {
        whenever(fallacyRepository.searchFallacies("test")).thenReturn(flowOf(listOf(testFallacy1)))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Set search
        viewModel.onSearchQueryChange("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Clear search
        viewModel.onSearchQueryChange("")

        viewModel.fallacies.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val fallacies = expectMostRecentItem()
            assertThat(fallacies).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteFallacy only deletes custom fallacies`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Try to delete non-custom fallacy
        viewModel.deleteFallacy(testFallacy1)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(fallacyRepository, never()).deleteFallacy(any())
    }

    @Test
    fun `deleteFallacy deletes custom fallacy`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteFallacy(customFallacy)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(fallacyRepository).deleteFallacy(customFallacy)
    }

    @Test
    fun `init syncs fallacies if database is empty`() = runTest {
        whenever(fallacyRepository.getAllFallaciesSync()).thenReturn(emptyList())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(fallacyRepository).insertFallacies(any())
    }

    @Test
    fun `init does not sync fallacies if database has data`() = runTest {
        whenever(fallacyRepository.getAllFallaciesSync()).thenReturn(listOf(testFallacy1))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(fallacyRepository, never()).insertFallacies(any())
    }

    @Test
    fun `blank search query returns all fallacies`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("   ")

        viewModel.fallacies.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val fallacies = expectMostRecentItem()
            assertThat(fallacies).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
