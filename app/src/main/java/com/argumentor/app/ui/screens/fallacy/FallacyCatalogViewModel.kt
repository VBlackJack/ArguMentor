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
        // MEMORY-001 FIX: Check and sync fallacies on init, then observe with lifecycle-aware collection
        viewModelScope.launch {
            // First check if database is empty and sync if needed (one-time operation)
            val existingFallacies = fallacyRepository.getAllFallaciesSync()
            if (existingFallacies.isEmpty()) {
                syncFallaciesFromResources()
            }
        }

        // MEMORY-001 FIX: Use stateIn with WhileSubscribed to avoid memory leaks
        // The Flow will only collect while there are active subscribers (UI visible)
        viewModelScope.launch {
            fallacyRepository.getAllFallacies()
                .map { fallaciesFromDb ->
                    // Update fallacies with localized content from strings.xml
                    fallaciesFromDb.map { fallacy ->
                        localizeFallacy(fallacy)
                    }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = emptyList()
                )
                .collect { localizedFallacies ->
                    _fallacies.value = localizedFallacies
                }
        }
    }

    /**
     * Helper function to localize a fallacy.
     * SMELL-003 FIX: Extracted duplication into reusable function.
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
            fallacyRepository.searchFallacies(query)
                .map { results ->
                    // SMELL-003 FIX: Use extracted localizeFallacy function
                    results.map { fallacy -> localizeFallacy(fallacy) }
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = emptyList()
                )
                .collect { localizedResults ->
                    _fallacies.value = localizedResults
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
