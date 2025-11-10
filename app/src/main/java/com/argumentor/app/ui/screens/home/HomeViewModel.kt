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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _allTopics = MutableStateFlow<List<Topic>>(emptyList())

    // New UiState-based approach
    private val _uiState = MutableStateFlow<UiState<List<Topic>>>(UiState.Initial)
    val uiState: StateFlow<UiState<List<Topic>>> = _uiState.asStateFlow()

    // Legacy properties for backward compatibility
    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var filterJob: Job? = null

    init {
        loadTopics()
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .collect {
                    _isLoading.value = it.isNotBlank() || _selectedTag.value != null
                    applyFilters()
                }
        }
    }

    private fun loadTopics() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                topicRepository.getAllTopics().collect { topicList ->
                    _allTopics.value = topicList
                    applyFilters()
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Une erreur inconnue s'est produite",
                    exception = e
                )
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // Don't set isLoading here - let the debounced observer handle it
    }

    fun onTagSelected(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
        _isLoading.value = true
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            try {
                var filteredTopics = _allTopics.value

                // Apply tag filter
                val tag = _selectedTag.value
                if (tag != null) {
                    filteredTopics = filteredTopics.filter { topic ->
                        topic.tags.contains(tag)
                    }
                }

                // Apply search filter
                val query = _searchQuery.value
                if (query.isNotBlank()) {
                    filteredTopics = filteredTopics.filter { topic ->
                        topic.title.contains(query, ignoreCase = true) ||
                        topic.summary.contains(query, ignoreCase = true) ||
                        topic.tags.any { it.contains(query, ignoreCase = true) }
                    }
                }

                _topics.value = filteredTopics

                // Update UiState based on results
                _uiState.value = if (filteredTopics.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(filteredTopics)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Une erreur s'est produite lors du filtrage",
                    exception = e
                )
            } finally {
                _isLoading.value = false
            }
        }
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
