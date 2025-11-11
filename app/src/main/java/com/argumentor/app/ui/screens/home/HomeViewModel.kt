package com.argumentor.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.repository.TopicRepository
import com.argumentor.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<UiState<List<Topic>>> = combine(
        topicRepository.getAllTopics(),
        _searchQuery.debounce(300),
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
        emit(UiState.Error(
            message = e.message ?: "Une erreur inconnue s'est produite",
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
