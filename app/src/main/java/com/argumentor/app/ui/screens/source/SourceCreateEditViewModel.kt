package com.argumentor.app.ui.screens.source

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Source
import com.argumentor.app.data.repository.ClaimRepository
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
class SourceCreateEditViewModel @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val evidenceRepository: EvidenceRepository,
    private val claimRepository: ClaimRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _citation = MutableStateFlow("")
    val citation: StateFlow<String> = _citation.asStateFlow()

    private val _url = MutableStateFlow("")
    val url: StateFlow<String> = _url.asStateFlow()

    private val _publisher = MutableStateFlow("")
    val publisher: StateFlow<String> = _publisher.asStateFlow()

    private val _date = MutableStateFlow("")
    val date: StateFlow<String> = _date.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    private val _linkedEvidences = MutableStateFlow<List<Evidence>>(emptyList())
    val linkedEvidences: StateFlow<List<Evidence>> = _linkedEvidences.asStateFlow()

    private val _linkedClaims = MutableStateFlow<List<Claim>>(emptyList())
    val linkedClaims: StateFlow<List<Claim>> = _linkedClaims.asStateFlow()

    private var sourceId: String? = null
    private var isEditMode = false

    fun loadSource(sourceId: String?) {
        if (sourceId == null) {
            isEditMode = false
            return
        }

        this.sourceId = sourceId
        isEditMode = true
        _isLoading.value = true

        viewModelScope.launch {
            val source = sourceRepository.getSourceById(sourceId).first()
            source?.let {
                _title.value = it.title
                _citation.value = it.citation ?: ""
                _url.value = it.url ?: ""
                _publisher.value = it.publisher ?: ""
                _date.value = it.date ?: ""
                _notes.value = it.notes ?: ""
            }
            _isLoading.value = false
        }

        // Load linked evidences and claims
        viewModelScope.launch {
            evidenceRepository.getEvidencesBySourceId(sourceId).collect { evidences ->
                _linkedEvidences.value = evidences

                // Load claims for these evidences
                val claimIds = evidences.map { it.claimId }.distinct()
                val claims = claimIds.mapNotNull { claimId ->
                    claimRepository.getClaimById(claimId).first()
                }
                _linkedClaims.value = claims
            }
        }
    }

    fun onTitleChange(value: String) {
        _title.value = value
    }

    fun onCitationChange(value: String) {
        _citation.value = value
    }

    fun onUrlChange(value: String) {
        _url.value = value
    }

    fun onPublisherChange(value: String) {
        _publisher.value = value
    }

    fun onDateChange(value: String) {
        _date.value = value
    }

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun saveSource(onSaved: () -> Unit) {
        if (_title.value.isBlank()) {
            _errorMessage.value = "Source title cannot be empty"
            return
        }

        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val source = if (isEditMode && sourceId != null) {
                    // Update existing source
                    val existingSource = sourceRepository.getSourceById(sourceId!!).first()
                    existingSource?.copy(
                        title = _title.value.trim(),
                        citation = _citation.value.trim().takeIf { it.isNotEmpty() },
                        url = _url.value.trim().takeIf { it.isNotEmpty() },
                        publisher = _publisher.value.trim().takeIf { it.isNotEmpty() },
                        date = _date.value.trim().takeIf { it.isNotEmpty() },
                        notes = _notes.value.trim().takeIf { it.isNotEmpty() },
                        updatedAt = com.argumentor.app.data.model.getCurrentIsoTimestamp()
                    )
                } else {
                    // Create new source
                    Source(
                        title = _title.value.trim(),
                        citation = _citation.value.trim().takeIf { it.isNotEmpty() },
                        url = _url.value.trim().takeIf { it.isNotEmpty() },
                        publisher = _publisher.value.trim().takeIf { it.isNotEmpty() },
                        date = _date.value.trim().takeIf { it.isNotEmpty() },
                        notes = _notes.value.trim().takeIf { it.isNotEmpty() }
                    )
                }

                source?.let {
                    if (isEditMode) {
                        sourceRepository.updateSource(it)
                    } else {
                        sourceRepository.insertSource(it)
                    }
                    onSaved()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save source: ${e.message}"
            }
        }
    }
}
