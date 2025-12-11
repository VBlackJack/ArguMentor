package com.argumentor.app.ui.screens.importexport

import app.cash.turbine.test
import com.argumentor.app.R
import com.argumentor.app.data.dto.ImportResult
import com.argumentor.app.data.repository.ImportExportRepository
import com.argumentor.app.util.ResourceProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ImportExportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var importExportRepository: ImportExportRepository
    private lateinit var resourceProvider: ResourceProvider
    private lateinit var viewModel: ImportExportViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        importExportRepository = mock()
        resourceProvider = mock()

        whenever(resourceProvider.getString(R.string.export_success)).thenReturn("Export successful")
        whenever(resourceProvider.getString(R.string.error_export)).thenReturn("Export failed")
        whenever(resourceProvider.getString(R.string.error_import)).thenReturn("Import failed")
        whenever(resourceProvider.getString(eq(R.string.import_success), any(), any())).thenReturn("Import successful: 5 created, 2 updated")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ImportExportViewModel {
        return ImportExportViewModel(
            importExportRepository = importExportRepository,
            resourceProvider = resourceProvider
        )
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel = createViewModel()
        assertThat(viewModel.state.value).isEqualTo(ImportExportState.Idle)
    }

    @Test
    fun `initial similarity threshold is 0_90`() = runTest {
        viewModel = createViewModel()
        assertThat(viewModel.similarityThreshold.value).isEqualTo(0.90)
    }

    @Test
    fun `setSimilarityThreshold updates threshold`() = runTest {
        viewModel = createViewModel()

        viewModel.setSimilarityThreshold(0.85)

        assertThat(viewModel.similarityThreshold.value).isEqualTo(0.85)
    }

    @Test
    fun `exportData sets Loading state then Success on success`() = runTest {
        whenever(importExportRepository.exportToJson(any())).thenReturn(Result.success(Unit))

        viewModel = createViewModel()
        val outputStream = ByteArrayOutputStream()

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(ImportExportState.Idle)

            viewModel.exportData(outputStream)
            assertThat(awaitItem()).isEqualTo(ImportExportState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertThat(successState).isInstanceOf(ImportExportState.Success::class.java)
            assertThat((successState as ImportExportState.Success).message).isEqualTo("Export successful")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `exportData sets Error state on failure`() = runTest {
        whenever(importExportRepository.exportToJson(any()))
            .thenReturn(Result.failure(RuntimeException("Export error")))

        viewModel = createViewModel()
        val outputStream = ByteArrayOutputStream()

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(ImportExportState.Idle)

            viewModel.exportData(outputStream)
            assertThat(awaitItem()).isEqualTo(ImportExportState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(ImportExportState.Error::class.java)
            assertThat((errorState as ImportExportState.Error).message).isEqualTo("Export error")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `importData sets ImportPreview state on success`() = runTest {
        val importResult = ImportResult(
            success = true,
            totalItems = 8,
            created = 5,
            updated = 2,
            duplicates = 1,
            nearDuplicates = 0,
            errors = 0,
            itemsForReview = emptyList()
        )
        whenever(importExportRepository.importFromJson(any(), any()))
            .thenReturn(Result.success(importResult))

        viewModel = createViewModel()
        val inputStream = ByteArrayInputStream("{}".toByteArray())

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(ImportExportState.Idle)

            viewModel.importData(inputStream)
            assertThat(awaitItem()).isEqualTo(ImportExportState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val previewState = awaitItem()
            assertThat(previewState).isInstanceOf(ImportExportState.ImportPreview::class.java)
            val preview = previewState as ImportExportState.ImportPreview
            assertThat(preview.result.created).isEqualTo(5)
            assertThat(preview.result.updated).isEqualTo(2)
            assertThat(preview.result.duplicates).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `importData sets Error state on failure`() = runTest {
        whenever(importExportRepository.importFromJson(any(), any()))
            .thenReturn(Result.failure(RuntimeException("Invalid JSON")))

        viewModel = createViewModel()
        val inputStream = ByteArrayInputStream("invalid".toByteArray())

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(ImportExportState.Idle)

            viewModel.importData(inputStream)
            assertThat(awaitItem()).isEqualTo(ImportExportState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(ImportExportState.Error::class.java)
            assertThat((errorState as ImportExportState.Error).message).isEqualTo("Invalid JSON")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `importData uses current similarity threshold`() = runTest {
        val importResult = ImportResult(
            success = true,
            totalItems = 1,
            created = 1,
            updated = 0,
            duplicates = 0,
            nearDuplicates = 0,
            errors = 0,
            itemsForReview = emptyList()
        )
        whenever(importExportRepository.importFromJson(any(), eq(0.75)))
            .thenReturn(Result.success(importResult))

        viewModel = createViewModel()
        viewModel.setSimilarityThreshold(0.75)

        val inputStream = ByteArrayInputStream("{}".toByteArray())
        viewModel.importData(inputStream)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(importExportRepository).importFromJson(any(), eq(0.75))
    }

    @Test
    fun `importDataFromString works with string input`() = runTest {
        val importResult = ImportResult(
            success = true,
            totalItems = 4,
            created = 3,
            updated = 1,
            duplicates = 0,
            nearDuplicates = 0,
            errors = 0,
            itemsForReview = emptyList()
        )
        whenever(importExportRepository.importFromJson(any(), any()))
            .thenReturn(Result.success(importResult))

        viewModel = createViewModel()

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(ImportExportState.Idle)

            viewModel.importDataFromString("{\"topics\":[]}")
            assertThat(awaitItem()).isEqualTo(ImportExportState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val previewState = awaitItem()
            assertThat(previewState).isInstanceOf(ImportExportState.ImportPreview::class.java)
            assertThat((previewState as ImportExportState.ImportPreview).result.created).isEqualTo(3)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setError sets Error state`() = runTest {
        viewModel = createViewModel()

        viewModel.setError("Custom error message")

        assertThat(viewModel.state.value).isInstanceOf(ImportExportState.Error::class.java)
        assertThat((viewModel.state.value as ImportExportState.Error).message).isEqualTo("Custom error message")
    }

    @Test
    fun `confirmImport transitions from ImportPreview to Success`() = runTest {
        val importResult = ImportResult(
            success = true,
            totalItems = 8,
            created = 5,
            updated = 2,
            duplicates = 1,
            nearDuplicates = 0,
            errors = 0,
            itemsForReview = emptyList()
        )
        whenever(importExportRepository.importFromJson(any(), any()))
            .thenReturn(Result.success(importResult))

        viewModel = createViewModel()
        val inputStream = ByteArrayInputStream("{}".toByteArray())

        viewModel.importData(inputStream)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.state.value).isInstanceOf(ImportExportState.ImportPreview::class.java)

        viewModel.confirmImport()

        assertThat(viewModel.state.value).isInstanceOf(ImportExportState.Success::class.java)
    }

    @Test
    fun `confirmImport does nothing if not in ImportPreview state`() = runTest {
        viewModel = createViewModel()

        viewModel.confirmImport() // Called while in Idle state

        assertThat(viewModel.state.value).isEqualTo(ImportExportState.Idle)
    }

    @Test
    fun `resetState sets state back to Idle`() = runTest {
        viewModel = createViewModel()
        viewModel.setError("Some error")

        assertThat(viewModel.state.value).isInstanceOf(ImportExportState.Error::class.java)

        viewModel.resetState()

        assertThat(viewModel.state.value).isEqualTo(ImportExportState.Idle)
    }

    @Test
    fun `DEFAULT_REVIEW_PAGE_SIZE is 50`() {
        assertThat(ImportExportViewModel.DEFAULT_REVIEW_PAGE_SIZE).isEqualTo(50)
    }
}
