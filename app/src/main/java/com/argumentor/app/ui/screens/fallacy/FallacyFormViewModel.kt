package com.argumentor.app.ui.screens.fallacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.repository.FallacyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Fallacy Form screen (create/edit).
 * Handles form state, validation, and saving fallacies.
 */
@HiltViewModel
class FallacyFormViewModel @Inject constructor(
    private val fallacyRepository: FallacyRepository
) : ViewModel() {

    private val _fallacyId = MutableStateFlow<String?>(null)
    val fallacyId: StateFlow<String?> = _fallacyId.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _example = MutableStateFlow("")
    val example: StateFlow<String> = _example.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _descriptionError = MutableStateFlow<String?>(null)
    val descriptionError: StateFlow<String?> = _descriptionError.asStateFlow()

    private val _exampleError = MutableStateFlow<String?>(null)
    val exampleError: StateFlow<String?> = _exampleError.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Loads an existing fallacy for editing.
     */
    fun loadFallacy(fallacyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val fallacy = fallacyRepository.getFallacyByIdSync(fallacyId)
                if (fallacy != null) {
                    _fallacyId.value = fallacy.id
                    _name.value = fallacy.name
                    _description.value = fallacy.description
                    _example.value = fallacy.example
                    _category.value = fallacy.category
                } else {
                    _error.value = "Fallacy not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load fallacy"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
        _nameError.value = null
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
        _descriptionError.value = null
    }

    fun onExampleChange(value: String) {
        _example.value = value
        _exampleError.value = null
    }

    fun onCategoryChange(value: String) {
        _category.value = value
    }

    /**
     * Validates the form and returns true if valid.
     */
    private fun validateForm(): Boolean {
        var isValid = true

        if (_name.value.isBlank()) {
            _nameError.value = "Le nom est obligatoire"
            isValid = false
        }

        if (_description.value.isBlank()) {
            _descriptionError.value = "La description est obligatoire"
            isValid = false
        }

        if (_example.value.isBlank()) {
            _exampleError.value = "L'exemple est obligatoire"
            isValid = false
        }

        return isValid
    }

    /**
     * Saves the fallacy (creates new or updates existing).
     */
    fun saveFallacy(onSuccess: () -> Unit) {
        if (!validateForm()) {
            return
        }

        _isSaving.value = true
        viewModelScope.launch {
            try {
                val fallacy = Fallacy(
                    id = _fallacyId.value ?: UUID.randomUUID().toString(),
                    name = _name.value.trim(),
                    description = _description.value.trim(),
                    example = _example.value.trim(),
                    category = _category.value.trim(),
                    isCustom = true
                )

                if (_fallacyId.value != null) {
                    // Update existing
                    fallacyRepository.updateFallacy(fallacy)
                } else {
                    // Create new
                    fallacyRepository.insertFallacy(fallacy)
                }

                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to save fallacy"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
