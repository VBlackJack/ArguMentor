package com.argumentor.app.ui.screens.importexport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.R
import com.argumentor.app.data.dto.ImportResult
import com.argumentor.app.data.repository.ImportExportRepository
import com.argumentor.app.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

sealed class ImportExportState {
    object Idle : ImportExportState()
    object Loading : ImportExportState()
    data class Success(val message: String) : ImportExportState()
    data class Error(val message: String) : ImportExportState()

    /**
     * Import preview state showing items that need user review.
     *
     * PERFORMANCE NOTE: The ImportResult may contain a large list of items for review
     * (itemsForReview). For imports with hundreds of near-duplicate items, the UI
     * should implement pagination or virtual scrolling to prevent performance issues.
     *
     * Recommendations for UI layer:
     * - Use LazyColumn with paging for displaying itemsForReview
     * - Consider limiting initial display to first 50-100 items
     * - Implement "Load More" button or infinite scroll for remaining items
     * - Add search/filter functionality to help users find specific review items
     */
    data class ImportPreview(val result: ImportResult) : ImportExportState()
}

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val importExportRepository: ImportExportRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _state = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val state: StateFlow<ImportExportState> = _state.asStateFlow()

    private val _similarityThreshold = MutableStateFlow(0.90)
    val similarityThreshold: StateFlow<Double> = _similarityThreshold.asStateFlow()

    companion object {
        /**
         * Default page size for displaying review items in the UI.
         * Used to prevent performance issues when rendering large lists of near-duplicates.
         */
        const val DEFAULT_REVIEW_PAGE_SIZE = 50

        /**
         * Timeout for import/export operations in milliseconds.
         * Prevents UI from hanging indefinitely on slow operations.
         */
        const val OPERATION_TIMEOUT_MS = 60_000L // 60 seconds
    }

    fun setSimilarityThreshold(threshold: Double) {
        _similarityThreshold.value = threshold
    }

    fun exportData(outputStream: OutputStream) {
        viewModelScope.launch {
            _state.value = ImportExportState.Loading

            try {
                withTimeout(OPERATION_TIMEOUT_MS) {
                    importExportRepository.exportToJson(outputStream).fold(
                        onSuccess = {
                            Timber.d("Data export successful")
                            _state.value = ImportExportState.Success(resourceProvider.getString(R.string.export_success))
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to export data")
                            _state.value = ImportExportState.Error(
                                error.message ?: resourceProvider.getString(R.string.error_export)
                            )
                        }
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Timber.e(e, "Export operation timed out")
                _state.value = ImportExportState.Error(
                    resourceProvider.getString(R.string.error_operation_timeout)
                )
            }
        }
    }

    fun importData(inputStream: InputStream) {
        viewModelScope.launch {
            _state.value = ImportExportState.Loading

            try {
                withTimeout(OPERATION_TIMEOUT_MS) {
                    importExportRepository.importFromJson(
                        inputStream,
                        _similarityThreshold.value
                    ).fold(
                        onSuccess = { result ->
                            Timber.d("Data import preview: ${result.created} created, ${result.updated} updated, ${result.duplicates} duplicates")
                            _state.value = ImportExportState.ImportPreview(result)
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to import data")
                            _state.value = ImportExportState.Error(
                                error.message ?: resourceProvider.getString(R.string.error_import)
                            )
                        }
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Timber.e(e, "Import operation timed out")
                _state.value = ImportExportState.Error(
                    resourceProvider.getString(R.string.error_operation_timeout)
                )
            }
        }
    }

    fun importDataFromString(jsonContent: String) {
        viewModelScope.launch {
            _state.value = ImportExportState.Loading

            // Create an InputStream from the string
            val inputStream = jsonContent.byteInputStream(Charsets.UTF_8)

            importExportRepository.importFromJson(
                inputStream,
                _similarityThreshold.value
            ).fold(
                onSuccess = { result ->
                    Timber.d("Data import from string preview: ${result.created} created, ${result.updated} updated, ${result.duplicates} duplicates")
                    _state.value = ImportExportState.ImportPreview(result)
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to import data from string")
                    _state.value = ImportExportState.Error(
                        error.message ?: resourceProvider.getString(R.string.error_import)
                    )
                }
            )
        }
    }

    fun setError(message: String) {
        _state.value = ImportExportState.Error(message)
    }

    fun confirmImport() {
        // Already imported in preview, just confirm
        val currentState = _state.value
        if (currentState is ImportExportState.ImportPreview) {
            val result = currentState.result
            Timber.d("Import confirmed: ${result.created} created, ${result.updated} updated")
            _state.value = ImportExportState.Success(
                resourceProvider.getString(R.string.import_success, result.created, result.updated)
            )
        }
    }

    fun resetState() {
        _state.value = ImportExportState.Idle
    }
}
