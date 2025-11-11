package com.argumentor.app.ui.screens.fallacy

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.constants.FallacyCatalog
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.repository.FallacyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Fallacy Detail screen.
 * Handles loading, updating, and deleting a specific fallacy.
 */
@HiltViewModel
class FallacyDetailViewModel @Inject constructor(
    private val application: Application,
    private val fallacyRepository: FallacyRepository
) : ViewModel() {

    private val _fallacy = MutableStateFlow<Fallacy?>(null)
    val fallacy: StateFlow<Fallacy?> = _fallacy.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Loads a fallacy by its ID.
     */
    fun loadFallacy(fallacyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                fallacyRepository.getFallacyById(fallacyId).collect { fallacyFromDb ->
                    if (fallacyFromDb != null) {
                        // Update with localized content from strings.xml
                        val localizedFallacy = FallacyCatalog.getFallacyById(application, fallacyFromDb.id)
                        _fallacy.value = if (localizedFallacy != null) {
                            fallacyFromDb.copy(
                                name = localizedFallacy.name,
                                description = localizedFallacy.description,
                                example = localizedFallacy.example
                            )
                        } else {
                            fallacyFromDb
                        }
                    } else {
                        _error.value = "Fallacy not found"
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes the current fallacy.
     * Only custom fallacies can be deleted.
     */
    fun deleteFallacy(onSuccess: () -> Unit) {
        val currentFallacy = _fallacy.value ?: return

        if (!currentFallacy.isCustom) {
            _error.value = "Cannot delete pre-loaded fallacies"
            return
        }

        viewModelScope.launch {
            try {
                fallacyRepository.deleteFallacy(currentFallacy)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete fallacy"
            }
        }
    }

    /**
     * Clears any error messages.
     */
    fun clearError() {
        _error.value = null
    }
}
