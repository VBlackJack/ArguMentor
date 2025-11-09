package com.argumentor.app.ui.screens.importexport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.dto.ImportResult
import com.argumentor.app.data.repository.ImportExportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

sealed class ImportExportState {
    object Idle : ImportExportState()
    object Loading : ImportExportState()
    data class Success(val message: String) : ImportExportState()
    data class Error(val message: String) : ImportExportState()
    data class ImportPreview(val result: ImportResult) : ImportExportState()
}

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val importExportRepository: ImportExportRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val state: StateFlow<ImportExportState> = _state.asStateFlow()

    private val _similarityThreshold = MutableStateFlow(0.90)
    val similarityThreshold: StateFlow<Double> = _similarityThreshold.asStateFlow()

    fun setSimilarityThreshold(threshold: Double) {
        _similarityThreshold.value = threshold
    }

    fun exportData(outputStream: OutputStream) {
        viewModelScope.launch {
            _state.value = ImportExportState.Loading

            importExportRepository.exportToJson(outputStream).fold(
                onSuccess = {
                    _state.value = ImportExportState.Success("Données exportées avec succès")
                },
                onFailure = { error ->
                    _state.value = ImportExportState.Error(
                        error.message ?: "Erreur lors de l'export"
                    )
                }
            )
        }
    }

    fun importData(inputStream: InputStream) {
        viewModelScope.launch {
            _state.value = ImportExportState.Loading

            importExportRepository.importFromJson(
                inputStream,
                _similarityThreshold.value
            ).fold(
                onSuccess = { result ->
                    _state.value = ImportExportState.ImportPreview(result)
                },
                onFailure = { error ->
                    _state.value = ImportExportState.Error(
                        error.message ?: "Erreur lors de l'import"
                    )
                }
            )
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
                    _state.value = ImportExportState.ImportPreview(result)
                },
                onFailure = { error ->
                    _state.value = ImportExportState.Error(
                        error.message ?: "Erreur lors de l'import"
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
            _state.value = ImportExportState.Success(
                "Import réussi: ${result.created} créés, ${result.updated} mis à jour, ${result.duplicates} doublons"
            )
        }
    }

    fun resetState() {
        _state.value = ImportExportState.Idle
    }
}
