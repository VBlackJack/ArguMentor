package com.argumentor.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
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

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
                    applyFilters()
                }
        }
    }

    private fun loadTopics() {
        viewModelScope.launch {
            topicRepository.getAllTopics().collect { topicList ->
                _allTopics.value = topicList
                applyFilters()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _isLoading.value = query.isNotBlank() || _selectedTag.value != null
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
            } finally {
                _isLoading.value = false
            }
        }
    }
}
