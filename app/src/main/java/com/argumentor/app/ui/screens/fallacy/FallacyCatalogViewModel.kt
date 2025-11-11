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
 * ViewModel for the Fallacy Catalog screen.
 * Manages the list of logical fallacies and search functionality.
 * Now uses Room database for persistent storage and CRUD operations.
 */
@HiltViewModel
class FallacyCatalogViewModel @Inject constructor(
    private val application: Application,
    private val fallacyRepository: FallacyRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fallacies = MutableStateFlow<List<Fallacy>>(emptyList())
    val fallacies: StateFlow<List<Fallacy>> = _fallacies.asStateFlow()

    init {
        // Load fallacies from database
        viewModelScope.launch {
            fallacyRepository.getAllFallacies().collect { fallaciesFromDb ->
                // If database is empty, sync with localized strings
                if (fallaciesFromDb.isEmpty()) {
                    syncFallaciesFromResources()
                } else {
                    // Update fallacies with localized content from strings.xml
                    _fallacies.value = fallaciesFromDb.map { fallacy ->
                        // Get localized name, description, and example from strings.xml
                        val localizedFallacy = FallacyCatalog.getFallacyById(application, fallacy.id)
                        if (localizedFallacy != null) {
                            fallacy.copy(
                                name = localizedFallacy.name,
                                description = localizedFallacy.description,
                                example = localizedFallacy.example
                            )
                        } else {
                            fallacy
                        }
                    }
                }
            }
        }
    }

    /**
     * Syncs fallacies from string resources to the database.
     * This is called on first launch or if the database is empty.
     */
    private suspend fun syncFallaciesFromResources() {
        val fallaciesFromResources = FallacyCatalog.getFallacies(application)
        val fallacyEntities = fallaciesFromResources.map { catalogFallacy ->
            Fallacy(
                id = catalogFallacy.id,
                name = catalogFallacy.name,
                description = catalogFallacy.description,
                example = catalogFallacy.example,
                category = "",
                isCustom = false
            )
        }
        fallacyRepository.insertFallacies(fallacyEntities)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            fallacyRepository.searchFallacies(query).collect { results ->
                // Update with localized content
                _fallacies.value = results.map { fallacy ->
                    val localizedFallacy = FallacyCatalog.getFallacyById(application, fallacy.id)
                    if (localizedFallacy != null) {
                        fallacy.copy(
                            name = localizedFallacy.name,
                            description = localizedFallacy.description,
                            example = localizedFallacy.example
                        )
                    } else {
                        fallacy
                    }
                }
            }
        }
    }

    /**
     * Deletes a fallacy from the database.
     * Only custom fallacies can be deleted.
     */
    fun deleteFallacy(fallacy: Fallacy) {
        if (!fallacy.isCustom) {
            // Prevent deletion of pre-loaded fallacies
            return
        }
        viewModelScope.launch {
            fallacyRepository.deleteFallacy(fallacy)
        }
    }
}
