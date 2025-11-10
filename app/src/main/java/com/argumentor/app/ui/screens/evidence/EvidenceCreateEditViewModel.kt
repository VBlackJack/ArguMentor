package com.argumentor.app.ui.screens.evidence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Source
import com.argumentor.app.data.repository.EvidenceRepository
import com.argumentor.app.data.repository.SourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EvidenceCreateEditViewModel @Inject constructor(
    private val evidenceRepository: EvidenceRepository,
    private val sourceRepository: SourceRepository
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

    private var evidenceId: String? = null
    private var claimId: String = ""
    private var isEditMode = false

    fun loadEvidence(evidenceId: String?, claimId: String) {
        this.claimId = claimId
        _isLoading.value = true

        // Load available sources
        viewModelScope.launch {
            try {
                sourceRepository.getAllSources().collect { sources ->
                    _availableSources.value = sources
                }
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
            return
        }

        viewModelScope.launch {
            val evidence = if (isEditMode && evidenceId != null) {
                // Update existing evidence
                evidenceRepository.getEvidenceById(evidenceId!!)?.copy(
                    content = _content.value.trim(),
                    type = _type.value,
                    quality = _quality.value,
                    sourceId = _selectedSource.value?.id
                )
            } else {
                // Create new evidence
                Evidence(
                    claimId = claimId,
                    content = _content.value.trim(),
                    type = _type.value,
                    quality = _quality.value,
                    sourceId = _selectedSource.value?.id
                )
            }

            evidence?.let {
                if (isEditMode) {
                    evidenceRepository.updateEvidence(it)
                } else {
                    evidenceRepository.insertEvidence(it)
                }
                onSaved()
            }
        }
    }

    fun deleteEvidence(onDeleted: () -> Unit) {
        if (!isEditMode || evidenceId == null) return

        viewModelScope.launch {
            evidenceRepository.getEvidenceById(evidenceId!!)?.let { evidence ->
                evidenceRepository.deleteEvidence(evidence)
                onDeleted()
            }
        }
    }
}
