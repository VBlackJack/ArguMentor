package com.argumentor.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.repository.TopicRepository
import com.argumentor.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Topic>>>(UiState.Initial)
    val uiState: StateFlow<UiState<List<Topic>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var topicsJob: Job? = null

    init {
        loadTopics()
        observeFilters()
    }

    @OptIn(FlowPreview::class)
    private fun observeFilters() {
        viewModelScope.launch {
            // Combine search query and tag changes
            combine(
                _searchQuery.debounce(300),  // Wait 300ms after user stops typing
                _selectedTag
            ) { query, tag ->
                Pair(query.takeIf { it.isNotBlank() }, tag)
            }.collect { _ ->
                loadTopics()
            }
        }
    }

    private fun loadTopics() {
        // Cancel previous job to avoid multiple simultaneous loads
        topicsJob?.cancel()

        topicsJob = viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                val query = _searchQuery.value.takeIf { it.isNotBlank() }
                val tag = _selectedTag.value

                // Use SQL-based filtering for much better performance
                topicRepository.getFilteredTopics(tag, query).collect { topics ->
                    _uiState.value = if (topics.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(topics)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Une erreur inconnue s'est produite",
                    exception = e
                )
            }
        }
    }

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
            try {
                // In a real app, this might fetch from a remote source
                // For now, just reload local data
                loadTopics()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun retry() {
        loadTopics()
    }
}
