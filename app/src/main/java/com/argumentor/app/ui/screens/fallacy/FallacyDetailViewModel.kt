package com.argumentor.app.ui.screens.fallacy

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.constants.FallacyCatalog
import com.argumentor.app.data.model.Fallacy
import com.argumentor.app.data.repository.FallacyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
     * MEMORY-002 FIX: Uses stateIn with WhileSubscribed to avoid memory leaks.
     */
    fun loadFallacy(fallacyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                fallacyRepository.getFallacyById(fallacyId)
                    .map { fallacyFromDb ->
                        if (fallacyFromDb != null) {
                            // Update with localized content from strings.xml
                            localizeFallacy(fallacyFromDb)
                        } else {
                            null
                        }
                    }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                        initialValue = null
                    )
                    .collect { localizedFallacy ->
                        if (localizedFallacy != null) {
                            _fallacy.value = localizedFallacy
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
     * Helper function to localize a fallacy.
     */
    private fun localizeFallacy(fallacy: Fallacy): Fallacy {
        val localizedFallacy = FallacyCatalog.getFallacyById(application, fallacy.id)
        return if (localizedFallacy != null) {
            fallacy.copy(
                name = localizedFallacy.name,
                description = localizedFallacy.description,
                example = localizedFallacy.example
            )
        } else {
            fallacy
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
