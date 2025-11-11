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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.argumentor.app.R
import com.argumentor.app.util.ResourceProvider
import timber.log.Timber

@HiltViewModel
class SourceCreateEditViewModel @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val evidenceRepository: EvidenceRepository,
    private val claimRepository: ClaimRepository,
    private val resourceProvider: ResourceProvider
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

    private val _reliabilityScore = MutableStateFlow(0)
    val reliabilityScore: StateFlow<Int> = _reliabilityScore.asStateFlow()

    private val _sourceId = MutableStateFlow<String?>(null)
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // Track initial values for hasUnsavedChanges()
    private val _initialTitle = MutableStateFlow("")
    private val _initialCitation = MutableStateFlow("")
    private val _initialUrl = MutableStateFlow("")
    private val _initialReliabilityScore = MutableStateFlow(0)

    fun loadSource(sourceId: String?) {
        if (sourceId == null) {
            _isEditMode.value = false
            // Reset to default values for new source
            _initialTitle.value = ""
            _initialCitation.value = ""
            _initialUrl.value = ""
            _initialReliabilityScore.value = 0
            return
        }

        _sourceId.value = sourceId
        _isEditMode.value = true
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

                // Track initial values for hasUnsavedChanges()
                _initialTitle.value = it.title
                _initialCitation.value = it.citation ?: ""
                _initialUrl.value = it.url ?: ""
                _initialReliabilityScore.value = 0
            }
            _isLoading.value = false
        }

        // Load linked evidences and claims
        viewModelScope.launch {
            combine(
                evidenceRepository.getEvidencesBySourceId(sourceId),
                claimRepository.getAllClaims()
            ) { evidences, allClaims ->
                _linkedEvidences.value = evidences

                // Filter claims by evidence claim IDs instead of N+1 queries
                val claimIds = evidences.map { it.claimId }.distinct().toSet()
                _linkedClaims.value = allClaims.filter { it.id in claimIds }
            }.collect { }
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
            _errorMessage.value = resourceProvider.getString(R.string.error_source_title_empty)
            return
        }

        _errorMessage.value = null
        _isSaving.value = true

        viewModelScope.launch {
            runCatching {
                val srcId = _sourceId.value
                val source = if (_isEditMode.value && srcId != null) {
                    // Update existing source
                    val existingSource = sourceRepository.getSourceById(srcId).first()
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
                    if (_isEditMode.value) {
                        sourceRepository.updateSource(it)
                        Timber.d("Source updated successfully: ${it.id}")
                    } else {
                        sourceRepository.insertSource(it)
                        Timber.d("Source created successfully: ${it.id}")
                    }
                    onSaved()
                }
            }.onSuccess {
                // Success is already handled in the block above
            }.onFailure { e ->
                Timber.e(e, "Failed to save source")
                _errorMessage.value = resourceProvider.getString(
                    R.string.error_save_source,
                    e.message ?: resourceProvider.getString(R.string.error_unknown)
                )
            }
            _isSaving.value = false
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return _title.value != _initialTitle.value ||
                _citation.value != _initialCitation.value ||
                _url.value != _initialUrl.value ||
                _reliabilityScore.value != _initialReliabilityScore.value
    }
}
