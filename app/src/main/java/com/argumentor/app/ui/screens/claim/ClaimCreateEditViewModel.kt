package com.argumentor.app.ui.screens.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import com.argumentor.app.data.repository.ClaimRepository
import com.argumentor.app.data.repository.FallacyRepository
import com.argumentor.app.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClaimCreateEditViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
    private val fallacyRepository: FallacyRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _stance = MutableStateFlow(Claim.Stance.NEUTRAL)
    val stance: StateFlow<Claim.Stance> = _stance.asStateFlow()

    private val _strength = MutableStateFlow(Claim.Strength.MEDIUM)
    val strength: StateFlow<Claim.Strength> = _strength.asStateFlow()

    private val _selectedTopics = MutableStateFlow<List<String>>(emptyList())
    val selectedTopics: StateFlow<List<String>> = _selectedTopics.asStateFlow()

    private val _selectedFallacies = MutableStateFlow<List<String>>(emptyList())
    val selectedFallacies: StateFlow<List<String>> = _selectedFallacies.asStateFlow()

    private val _allFallacies = MutableStateFlow<List<Fallacy>>(emptyList())
    val allFallacies: StateFlow<List<Fallacy>> = _allFallacies.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    private val _claimId = MutableStateFlow<String?>(null)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Track initial values to detect unsaved changes
    private val _initialText = MutableStateFlow("")
    private val _initialStance = MutableStateFlow(Claim.Stance.NEUTRAL)
    private val _initialStrength = MutableStateFlow(Claim.Strength.MEDIUM)
    private val _initialTopics = MutableStateFlow<List<String>>(emptyList())
    private val _initialFallacies = MutableStateFlow<List<String>>(emptyList())

    init {
        // Load all fallacies from database for selection
        viewModelScope.launch {
            fallacyRepository.getAllFallacies().collect { fallacies ->
                _allFallacies.value = fallacies
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadClaim(claimId: String?, topicId: String?) {
        topicId?.let {
            _selectedTopics.value = listOf(it)
            _initialTopics.value = listOf(it)
        }

        if (claimId == null) {
            _isEditMode.value = false
            // Reset initial values for new claim
            _initialText.value = ""
            _initialStance.value = Claim.Stance.NEUTRAL
            _initialStrength.value = Claim.Strength.MEDIUM
            _initialFallacies.value = emptyList()
            if (topicId == null) {
                _initialTopics.value = emptyList()
            }
            return
        }

        _isEditMode.value = true
        _claimId.value = claimId

        _isLoading.value = true
        viewModelScope.launch {
            try {
                claimRepository.getClaimByIdSync(claimId)?.let { claim ->
                    _text.value = claim.text
                    _stance.value = claim.stance
                    _strength.value = claim.strength
                    _selectedTopics.value = claim.topics
                    _selectedFallacies.value = claim.fallacyIds

                    // Store initial values
                    _initialText.value = claim.text
                    _initialStance.value = claim.stance
                    _initialStrength.value = claim.strength
                    _initialTopics.value = claim.topics
                    _initialFallacies.value = claim.fallacyIds
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load claim: $claimId")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return _text.value != _initialText.value ||
               _stance.value != _initialStance.value ||
               _strength.value != _initialStrength.value ||
               _selectedTopics.value != _initialTopics.value ||
               _selectedFallacies.value != _initialFallacies.value
    }

    fun onTextChange(newText: String) {
        _text.value = newText
    }

    fun onStanceChange(newStance: Claim.Stance) {
        _stance.value = newStance
    }

    fun onStrengthChange(newStrength: Claim.Strength) {
        _strength.value = newStrength
    }

    fun addTopic(topicId: String) {
        if (!_selectedTopics.value.contains(topicId)) {
            _selectedTopics.value = _selectedTopics.value + topicId
        }
    }

    fun removeTopic(topicId: String) {
        _selectedTopics.value = _selectedTopics.value.filter { it != topicId }
    }

    fun addFallacy(fallacyId: String) {
        if (!_selectedFallacies.value.contains(fallacyId)) {
            _selectedFallacies.value = _selectedFallacies.value + fallacyId
        }
    }

    fun removeFallacy(fallacyId: String) {
        _selectedFallacies.value = _selectedFallacies.value.filter { it != fallacyId }
    }

    fun saveClaim(onSaved: () -> Unit) {
        // VAL-001: Validate claim text is not blank
        if (_text.value.isBlank()) {
            _errorMessage.value = resourceProvider.getString(R.string.error_claim_text_empty)
            return
        }

        // VAL-001: Validate claim text length doesn't exceed maximum
        if (_text.value.length > com.argumentor.app.util.ValidationUtils.MAX_LONG_TEXT_LENGTH) {
            _errorMessage.value = resourceProvider.getString(R.string.error_text_too_long)
            return
        }

        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            runCatching {
                val claimId = _claimId.value
                if (_isEditMode.value && claimId != null) {
                    // BUG-005 FIX: Add proper null check to prevent creating new claim
                    // if the original claim was deleted between loading and saving
                    val existingClaim = claimRepository.getClaimByIdSync(claimId)

                    // If claim was deleted, throw an error instead of creating a new one
                    if (existingClaim == null) {
                        throw IllegalStateException(
                            "Cannot update claim: Claim with ID $claimId no longer exists. " +
                            "It may have been deleted by another process."
                        )
                    }

                    val claim = Claim(
                        id = claimId,
                        text = _text.value,
                        stance = _stance.value,
                        strength = _strength.value,
                        topics = _selectedTopics.value,
                        fallacyIds = _selectedFallacies.value,
                        createdAt = existingClaim.createdAt,
                        updatedAt = getCurrentIsoTimestamp()
                    )
                    claimRepository.updateClaim(claim)
                    Timber.d("Claim updated successfully: $claimId")
                } else {
                    // Create new claim
                    val claim = Claim(
                        text = _text.value,
                        stance = _stance.value,
                        strength = _strength.value,
                        topics = _selectedTopics.value,
                        fallacyIds = _selectedFallacies.value
                    )
                    claimRepository.insertClaim(claim)
                    Timber.d("Claim created successfully: ${claim.id}")
                }
            }.onSuccess {
                onSaved()
            }.onFailure { e ->
                Timber.e(e, "Failed to save claim")
                _errorMessage.value = resourceProvider.getString(
                    R.string.error_save_claim,
                    e.message ?: resourceProvider.getString(R.string.error_unknown)
                )
            }
            _isSaving.value = false
        }
    }
}
