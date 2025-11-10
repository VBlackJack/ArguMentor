package com.argumentor.app.ui.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import com.argumentor.app.data.repository.TagRepository
import com.argumentor.app.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicCreateEditViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _summary = MutableStateFlow("")
    val summary: StateFlow<String> = _summary.asStateFlow()

    private val _posture = MutableStateFlow(Topic.Posture.NEUTRAL_CRITICAL)
    val posture: StateFlow<Topic.Posture> = _posture.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    private val _topicId = MutableStateFlow<String?>(null)

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadTopic(topicId: String?) {
        if (topicId == null) {
            _isEditMode.value = false
            return
        }

        _isEditMode.value = true
        _topicId.value = topicId

        viewModelScope.launch {
            topicRepository.getTopicByIdSync(topicId)?.let { topic ->
                _title.value = topic.title
                _summary.value = topic.summary
                _posture.value = topic.posture
                _tags.value = topic.tags
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _title.value = newTitle
    }

    fun onSummaryChange(newSummary: String) {
        _summary.value = newSummary
    }

    fun onPostureChange(newPosture: Topic.Posture) {
        _posture.value = newPosture
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && !_tags.value.contains(tag)) {
            _tags.value = _tags.value + tag
        }
    }

    fun removeTag(tag: String) {
        _tags.value = _tags.value.filter { it != tag }
    }

    fun saveTopic(onSaved: () -> Unit) {
        if (_title.value.isBlank()) {
            _errorMessage.value = "Topic title cannot be empty"
            return
        }

        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                if (_isEditMode.value && _topicId.value != null) {
                    // Update existing topic
                    val existingTopic = topicRepository.getTopicByIdSync(_topicId.value!!)
                    val topic = Topic(
                        id = _topicId.value!!,
                        title = _title.value,
                        summary = _summary.value,
                        posture = _posture.value,
                        tags = _tags.value,
                        createdAt = existingTopic?.createdAt ?: getCurrentIsoTimestamp(),
                        updatedAt = getCurrentIsoTimestamp()
                    )
                    topicRepository.updateTopic(topic)
                } else {
                    // Create new topic
                    val topic = Topic(
                        title = _title.value,
                        summary = _summary.value,
                        posture = _posture.value,
                        tags = _tags.value
                    )
                    topicRepository.insertTopic(topic)
                }
                onSaved()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save topic: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
