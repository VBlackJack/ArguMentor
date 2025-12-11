package com.argumentor.app.data.repository

import app.cash.turbine.test
import com.argumentor.app.data.local.dao.SourceDao
import com.argumentor.app.data.model.Source
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SourceRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var sourceDao: SourceDao
    private lateinit var repository: SourceRepository

    private val testSource = Source(
        id = "source-1",
        title = "Test Source",
        citation = "Author, A. (2024). Test Source. Publisher.",
        publisher = "Test Publisher",
        date = "2024",
        url = "https://example.com",
        type = Source.Type.ACADEMIC,
        reliabilityScore = 0.9f
    )

    private val testSource2 = Source(
        id = "source-2",
        title = "Another Source",
        publisher = "Another Publisher"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sourceDao = mock()
        repository = SourceRepository(sourceDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllSources returns flow from dao`() = runTest {
        val sources = listOf(testSource, testSource2)
        whenever(sourceDao.getAllSources()).thenReturn(flowOf(sources))

        repository.getAllSources().test {
            assertThat(awaitItem()).isEqualTo(sources)
            awaitComplete()
        }
    }

    @Test
    fun `getSourceById returns flow from dao`() = runTest {
        whenever(sourceDao.observeSourceById("source-1")).thenReturn(flowOf(testSource))

        repository.getSourceById("source-1").test {
            assertThat(awaitItem()).isEqualTo(testSource)
            awaitComplete()
        }
    }

    @Test
    fun `getSourceById returns null for non-existent`() = runTest {
        whenever(sourceDao.observeSourceById("non-existent")).thenReturn(flowOf(null))

        repository.getSourceById("non-existent").test {
            assertThat(awaitItem()).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `getSourceByIdSync returns source from dao`() = runTest {
        whenever(sourceDao.getSourceById("source-1")).thenReturn(testSource)

        val result = repository.getSourceByIdSync("source-1")

        assertThat(result).isEqualTo(testSource)
    }

    @Test
    fun `getSourceByIdSync returns null for non-existent`() = runTest {
        whenever(sourceDao.getSourceById("non-existent")).thenReturn(null)

        val result = repository.getSourceByIdSync("non-existent")

        assertThat(result).isNull()
    }

    @Test
    fun `insertSource calls dao insert`() = runTest {
        repository.insertSource(testSource)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceDao).insertSource(testSource)
    }

    @Test
    fun `updateSource calls dao update`() = runTest {
        val updatedSource = testSource.copy(title = "Updated Title")

        repository.updateSource(updatedSource)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceDao).updateSource(updatedSource)
    }

    @Test
    fun `deleteSource calls dao delete`() = runTest {
        repository.deleteSource(testSource)

        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceDao).deleteSource(testSource)
    }

    @Test
    fun `deleteSourceById calls dao`() = runTest {
        repository.deleteSourceById("source-1")

        testDispatcher.scheduler.advanceUntilIdle()

        verify(sourceDao).deleteSourceById("source-1")
    }

    @Test
    fun `searchSources uses FTS search`() = runTest {
        val sources = listOf(testSource)
        whenever(sourceDao.searchSourcesFts("test")).thenReturn(flowOf(sources))

        repository.searchSources("test").test {
            assertThat(awaitItem()).isEqualTo(sources)
            awaitComplete()
        }
    }
}
