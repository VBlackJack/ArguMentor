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

    fun loadClaim(claimId: String?, topicId: String?) {
        topicId?.let {
            _selectedTopics.value = listOf(it)
        }

        if (claimId == null) {
            _isEditMode.value = false
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
            }
        }
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
        if (_text.value.isBlank()) return

        _isSaving.value = true

        viewModelScope.launch {
            try {
                if (_isEditMode.value && _claimId.value != null) {
                    // Update existing claim
                    val claim = Claim(
                        id = _claimId.value!!,
                        text = _text.value,
                        stance = _stance.value,
                        strength = _strength.value,
                        topics = _selectedTopics.value,
                        createdAt = "",
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
            } finally {
                _isSaving.value = false
            }
        }
    }
}
