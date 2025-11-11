package com.argumentor.app.ui.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.R
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.model.Topic
import com.argumentor.app.data.model.getCurrentIsoTimestamp
import com.argumentor.app.data.repository.TagRepository
import com.argumentor.app.data.repository.TopicRepository
import com.argumentor.app.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicCreateEditViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val tagRepository: TagRepository,
    private val settingsDataStore: SettingsDataStore,
    private val resourceProvider: ResourceProvider
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Track initial values to detect unsaved changes
    private val _initialTitle = MutableStateFlow("")
    private val _initialSummary = MutableStateFlow("")
    private val _initialPosture = MutableStateFlow(Topic.Posture.NEUTRAL_CRITICAL)
    private val _initialTags = MutableStateFlow<List<String>>(emptyList())

    // Store the default posture from settings
    private val _defaultPosture = MutableStateFlow(Topic.Posture.NEUTRAL_CRITICAL)

    init {
        // Load default posture from settings
        // BUGFIX: Use take(1) to collect only the initial value and avoid infinite collection
        // in the init block which can cause memory leaks and unnecessary recomputations
        viewModelScope.launch {
            settingsDataStore.defaultPosture
                .take(1)  // Only take the first emission
                .collect { postureString ->
                    val posture = Topic.Posture.fromString(postureString)
                    _defaultPosture.value = posture
                    // Update current posture and initial posture if we're not in edit mode
                    if (!_isEditMode.value) {
                        _posture.value = posture
                        _initialPosture.value = posture
                    }
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadTopic(topicId: String?) {
        if (topicId == null) {
            _isEditMode.value = false
            // Reset to default values for new topic
            _title.value = ""
            _summary.value = ""
            _posture.value = _defaultPosture.value
            _tags.value = emptyList()
            // Set initial values for new topic
            _initialTitle.value = ""
            _initialSummary.value = ""
            _initialPosture.value = _defaultPosture.value
            _initialTags.value = emptyList()
            return
        }

        _isEditMode.value = true
        _topicId.value = topicId
        _isLoading.value = true

        viewModelScope.launch {
            try {
                topicRepository.getTopicByIdSync(topicId)?.let { topic ->
                    _title.value = topic.title
                    _summary.value = topic.summary
                    _posture.value = topic.posture
                    _tags.value = topic.tags

                    // Store initial values
                    _initialTitle.value = topic.title
                    _initialSummary.value = topic.summary
                    _initialPosture.value = topic.posture
                    _initialTags.value = topic.tags

                    Timber.d("Topic loaded successfully: $topicId")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load topic")
                _errorMessage.value = resourceProvider.getString(R.string.error_unknown)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return _title.value != _initialTitle.value ||
               _summary.value != _initialSummary.value ||
               _posture.value != _initialPosture.value ||
               _tags.value != _initialTags.value
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
            _errorMessage.value = resourceProvider.getString(R.string.error_topic_title_empty)
            return
        }

        _isSaving.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            runCatching {
                val topicId = _topicId.value
                if (_isEditMode.value && topicId != null) {
                    // Update existing topic
                    val existingTopic = topicRepository.getTopicByIdSync(topicId)
                    val topic = Topic(
                        id = topicId,
                        title = _title.value,
                        summary = _summary.value,
                        posture = _posture.value,
                        tags = _tags.value,
                        createdAt = existingTopic?.createdAt ?: getCurrentIsoTimestamp(),
                        updatedAt = getCurrentIsoTimestamp()
                    )
                    topicRepository.updateTopic(topic)
                    Timber.d("Topic updated successfully: $topicId")
                } else {
                    // Create new topic
                    val topic = Topic(
                        title = _title.value,
                        summary = _summary.value,
                        posture = _posture.value,
                        tags = _tags.value
                    )
                    topicRepository.insertTopic(topic)
                    Timber.d("Topic created successfully: ${topic.id}")
                }
            }.onSuccess {
                onSaved()
            }.onFailure { e ->
                Timber.e(e, "Failed to save topic")
                _errorMessage.value = resourceProvider.getString(
                    R.string.error_save_topic,
                    e.message ?: resourceProvider.getString(R.string.error_unknown)
                )
            }
            _isSaving.value = false
        }
    }
}
