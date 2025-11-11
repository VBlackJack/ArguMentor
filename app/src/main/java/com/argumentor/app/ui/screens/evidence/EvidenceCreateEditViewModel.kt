package com.argumentor.app.ui.screens.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.R
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Source
import com.argumentor.app.data.repository.EvidenceRepository
import com.argumentor.app.data.repository.SourceRepository
import com.argumentor.app.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EvidenceCreateEditViewModel @Inject constructor(
    private val evidenceRepository: EvidenceRepository,
    private val sourceRepository: SourceRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _type = MutableStateFlow(Evidence.EvidenceType.EXAMPLE)
    val type: StateFlow<Evidence.EvidenceType> = _type.asStateFlow()

    private val _quality = MutableStateFlow(Evidence.Quality.MEDIUM)
    val quality: StateFlow<Evidence.Quality> = _quality.asStateFlow()

    private val _selectedSource = MutableStateFlow<Source?>(null)
    val selectedSource: StateFlow<Source?> = _selectedSource.asStateFlow()

    private val _availableSources = MutableStateFlow<List<Source>>(emptyList())
    val availableSources: StateFlow<List<Source>> = _availableSources.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    private var evidenceId: String? = null
    private var claimId: String = ""
    private var isEditMode = false

    fun loadEvidence(evidenceId: String?, claimId: String) {
        this.claimId = claimId
        _isLoading.value = true

        // Load available sources
        viewModelScope.launch {
            try {
                val sources = sourceRepository.getAllSources().first()
                _availableSources.value = sources
            } finally {
                if (evidenceId == null) {
                    _isLoading.value = false
                }
            }
        }

        if (evidenceId == null) {
            isEditMode = false
            return
        }

        this.evidenceId = evidenceId
        isEditMode = true

        viewModelScope.launch {
            evidenceRepository.getEvidenceById(evidenceId)?.let { evidence ->
                _content.value = evidence.content
                _type.value = evidence.type
                _quality.value = evidence.quality

                // Load selected source if exists
                evidence.sourceId?.let { sourceId ->
                    _selectedSource.value = sourceRepository.getSourceById(sourceId).first()
                }
            }
            _isLoading.value = false
        }
    }

    fun onContentChange(value: String) {
        _content.value = value
    }

    fun onTypeChange(value: Evidence.EvidenceType) {
        _type.value = value
    }

    fun onQualityChange(value: Evidence.Quality) {
        _quality.value = value
    }

    fun onSourceSelected(source: Source?) {
        _selectedSource.value = source
    }

    fun saveEvidence(onSaved: () -> Unit) {
        if (_content.value.isBlank()) {
            _errorMessage.value = resourceProvider.getString(R.string.error_evidence_content_empty)
            return
        }

        _errorMessage.value = null

        viewModelScope.launch {
            runCatching {
                // Validate that selected source still exists if one is selected
                val validatedSourceId = _selectedSource.value?.id?.let { sourceId ->
                    val sourceExists = sourceRepository.getSourceById(sourceId).first() != null
                    if (sourceExists) sourceId else null
                }

                val evId = evidenceId
                val evidence = if (isEditMode && evId != null) {
                    // Update existing evidence
                    evidenceRepository.getEvidenceById(evId)?.copy(
                        content = _content.value.trim(),
                        type = _type.value,
                        quality = _quality.value,
                        sourceId = validatedSourceId,
                        updatedAt = com.argumentor.app.data.model.getCurrentIsoTimestamp()
                    )
                } else {
                    // Create new evidence
                    Evidence(
                        claimId = claimId,
                        content = _content.value.trim(),
                        type = _type.value,
                        quality = _quality.value,
                        sourceId = validatedSourceId
                    )
                }

                evidence?.let {
                    if (isEditMode) {
                        evidenceRepository.updateEvidence(it)
                        Timber.d("Evidence updated successfully: ${it.id}")
                    } else {
                        evidenceRepository.insertEvidence(it)
                        Timber.d("Evidence created successfully")
                    }
                    onSaved()
                }
            }.onFailure { e ->
                Timber.e(e, "Failed to save evidence")
                _errorMessage.value = resourceProvider.getString(
                    R.string.error_save_evidence,
                    e.message ?: resourceProvider.getString(R.string.error_unknown)
                )
            }
        }
    }

    fun deleteEvidence(onDeleted: () -> Unit) {
        val evId = evidenceId
        if (!isEditMode || evId == null) return

        viewModelScope.launch {
            evidenceRepository.getEvidenceById(evId)?.let { evidence ->
                evidenceRepository.deleteEvidence(evidence)
                onDeleted()
            }
        }
    }
}
