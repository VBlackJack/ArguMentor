package com.argumentor.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.R
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.repository.TopicRepository
import com.argumentor.app.ui.common.UiState
import com.argumentor.app.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    companion object {
        /**
         * Debounce delay for search queries in milliseconds.
         * Prevents excessive filtering while user is still typing.
         */
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /**
     * Combined UI state that applies filtering based on search query and selected tag.
     *
     * Note on ViewModel vs Repository filtering:
     * This filtering logic is INTENTIONALLY in the ViewModel (not Repository) because:
     * 1. It's PRESENTATION logic - combines multiple UI state sources (searchQuery, selectedTag)
     * 2. It's USER-DRIVEN - uses debounce for better UX (300ms delay on search)
     * 3. It's REACTIVE - updates automatically when any input changes
     * 4. It's TESTABLE - can be unit tested independently of the UI
     *
     * Repository filtering would be appropriate if:
     * - Filtering was based on business rules (not UI state)
     * - Results needed to be reused across multiple ViewModels
     * - Database-level filtering was required for performance
     *
     * Current approach follows MVVM pattern: Repository provides data, ViewModel transforms
     * it for presentation. For simple in-memory filtering on small datasets (typical use case
     * here), ViewModel filtering is more appropriate and maintains separation of concerns.
     */
    @OptIn(FlowPreview::class)
    val uiState: StateFlow<UiState<List<Topic>>> = combine(
        topicRepository.getAllTopics().distinctUntilChanged(),  // PERFORMANCE: Avoid reprocessing identical lists
        _searchQuery.debounce(SEARCH_DEBOUNCE_MS),
        _selectedTag
    ) { topics, query, tag ->
        var filteredTopics = topics

        // Apply tag filter
        if (tag != null) {
            filteredTopics = filteredTopics.filter { topic ->
                topic.tags.contains(tag)
            }
        }

        // Apply search filter
        if (query.isNotBlank()) {
            filteredTopics = filteredTopics.filter { topic ->
                topic.title.contains(query, ignoreCase = true) ||
                topic.summary.contains(query, ignoreCase = true) ||
                topic.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

        filteredTopics
    }
    .map { topics ->
        if (topics.isEmpty()) {
            UiState.Empty
        } else {
            UiState.Success(topics)
        }
    }
    .catch { e ->
        Timber.e(e, "Error loading topics")
        emit(UiState.Error(
            message = e.message ?: resourceProvider.getString(R.string.error_unknown),
            exception = e
        ))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTagSelected(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun deleteTopic(topic: Topic, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            topicRepository.deleteTopic(topic)
            onComplete()
        }
    }

    fun restoreTopic(topic: Topic) {
        viewModelScope.launch {
            topicRepository.insertTopic(topic)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // The Flow will automatically update with latest data from repository
            // Just need to manage the refreshing state for UI
            try {
                // Small delay to ensure UI shows refresh indicator
                kotlinx.coroutines.delay(300)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun retry() {
        // No-op: The Flow automatically retries on collection
        // UI state is managed by the stateIn operator
    }
}
