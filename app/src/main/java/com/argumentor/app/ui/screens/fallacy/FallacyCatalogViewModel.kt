package com.argumentor.app.ui.screens.fallacy

import androidx.lifecycle.ViewModel
import com.argumentor.app.data.constants.FallacyCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the Fallacy Catalog screen.
 * Manages the list of logical fallacies and search functionality.
 */
@HiltViewModel
class FallacyCatalogViewModel @Inject constructor() : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fallacies = MutableStateFlow(FallacyCatalog.FALLACIES)
    val fallacies: StateFlow<List<FallacyCatalog.Fallacy>> = _fallacies.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _fallacies.value = if (query.isEmpty()) {
            FallacyCatalog.FALLACIES
        } else {
            FallacyCatalog.searchFallacies(query)
        }
    }
}
