package com.argumentor.app.ui.screens.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import com.argumentor.app.data.repository.ClaimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClaimCreateEditViewModel @Inject constructor(
    private val claimRepository: ClaimRepository
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _stance = MutableStateFlow(Claim.Stance.NEUTRAL)
    val stance: StateFlow<Claim.Stance> = _stance.asStateFlow()

    private val _strength = MutableStateFlow(Claim.Strength.MEDIUM)
    val strength: StateFlow<Claim.Strength> = _strength.asStateFlow()

    private val _selectedTopics = MutableStateFlow<List<String>>(emptyList())
    val selectedTopics: StateFlow<List<String>> = _selectedTopics.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    private val _claimId = MutableStateFlow<String?>(null)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Track initial values to detect unsaved changes
    private val _initialText = MutableStateFlow("")
    private val _initialStance = MutableStateFlow(Claim.Stance.NEUTRAL)
    private val _initialStrength = MutableStateFlow(Claim.Strength.MEDIUM)
    private val _initialTopics = MutableStateFlow<List<String>>(emptyList())

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
            if (topicId == null) {
                _initialTopics.value = emptyList()
            }
            return
        }

        _isEditMode.value = true
        _claimId.value = claimId

        viewModelScope.launch {
            claimRepository.getClaimByIdSync(claimId)?.let { claim ->
                _text.value = claim.text
                _stance.value = claim.stance
                _strength.value = claim.strength
                _selectedTopics.value = claim.topics

                // Store initial values
                _initialText.value = claim.text
                _initialStance.value = claim.stance
                _initialStrength.value = claim.strength
                _initialTopics.value = claim.topics
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return _text.value != _initialText.value ||
               _stance.value != _initialStance.value ||
               _strength.value != _initialStrength.value ||
               _selectedTopics.value != _initialTopics.value
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

    fun saveClaim(onSaved: () -> Unit) {
        if (_text.value.isBlank()) {
            _errorMessage.value = "Claim text cannot be empty"
            return
        }

        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val claimId = _claimId.value
                if (_isEditMode.value && claimId != null) {
                    // Update existing claim
                    val existingClaim = claimRepository.getClaimByIdSync(claimId)
                    val claim = Claim(
                        id = claimId,
                        text = _text.value,
                        stance = _stance.value,
                        strength = _strength.value,
                        topics = _selectedTopics.value,
                        createdAt = existingClaim?.createdAt ?: getCurrentIsoTimestamp(),
                        updatedAt = getCurrentIsoTimestamp()
                    )
                    claimRepository.updateClaim(claim)
                } else {
                    // Create new claim
                    val claim = Claim(
                        text = _text.value,
                        stance = _stance.value,
                        strength = _strength.value,
                        topics = _selectedTopics.value
                    )
                    claimRepository.insertClaim(claim)
                }
                onSaved()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save claim: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
