package com.argumentor.app.ui.screens.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Question
import com.argumentor.app.data.repository.ClaimRepository
import com.argumentor.app.data.repository.QuestionRepository
import com.argumentor.app.data.repository.TopicRepository
import com.argumentor.app.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QuestionCreateEditViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val claimRepository: ClaimRepository,
    private val topicRepository: TopicRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _kind = MutableStateFlow(Question.QuestionKind.CLARIFYING)
    val kind: StateFlow<Question.QuestionKind> = _kind.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    private val _availableClaims = MutableStateFlow<List<Claim>>(emptyList())
    val availableClaims: StateFlow<List<Claim>> = _availableClaims.asStateFlow()

    private val _selectedClaim = MutableStateFlow<Claim?>(null)
    val selectedClaim: StateFlow<Claim?> = _selectedClaim.asStateFlow()

    private val _isTopicLevel = MutableStateFlow(true)
    val isTopicLevel: StateFlow<Boolean> = _isTopicLevel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _questionId = MutableStateFlow<String?>(null)
    val questionId: StateFlow<String?> = _questionId.asStateFlow()

    private val _initialText = MutableStateFlow("")
    val initialText: StateFlow<String> = _initialText.asStateFlow()

    private val _initialKind = MutableStateFlow(Question.QuestionKind.CLARIFYING)
    val initialKind: StateFlow<Question.QuestionKind> = _initialKind.asStateFlow()

    private val _initialTargetId = MutableStateFlow<String?>(null)
    val initialTargetId: StateFlow<String?> = _initialTargetId.asStateFlow()

    private var targetId: String? = null
    private var initialTopicId: String? = null

    fun loadQuestion(questionId: String?, targetId: String?) {
        this.targetId = targetId
        this.initialTopicId = targetId

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (questionId != null) {
                    // Editing existing question
                    _questionId.value = questionId
                    questionRepository.getQuestionById(questionId)?.let { question ->
                        _text.value = question.text
                        _kind.value = question.kind
                        _initialText.value = question.text
                        _initialKind.value = question.kind
                        _initialTargetId.value = question.targetId

                        // Check if question targets a claim or topic
                        val claim = claimRepository.getClaimByIdSync(question.targetId)
                        if (claim != null) {
                            // Question targets a claim
                            _selectedClaim.value = claim
                            _isTopicLevel.value = false
                            // Find topic from claim to load other claims
                            if (claim.topics.isNotEmpty()) {
                                loadClaimsForTopic(claim.topics.first())
                            }
                        } else {
                            // Question targets a topic
                            _isTopicLevel.value = true
                            loadClaimsForTopic(question.targetId)
                        }
                    }
                } else if (targetId != null) {
                    // Creating new question - check if targetId is claim or topic
                    val claim = claimRepository.getClaimByIdSync(targetId)
                    if (claim != null) {
                        // Creating question for a claim
                        _selectedClaim.value = claim
                        _isTopicLevel.value = false
                        if (claim.topics.isNotEmpty()) {
                            loadClaimsForTopic(claim.topics.first())
                        }
                    } else {
                        // Creating question for a topic
                        _isTopicLevel.value = true
                        loadClaimsForTopic(targetId)
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadClaimsForTopic(topicId: String) {
        initialTopicId = topicId
        claimRepository.getAllClaims().first().let { allClaims ->
            _availableClaims.value = allClaims.filter { it.topics.contains(topicId) }
        }
    }

    fun onTextChange(newText: String) {
        _text.value = newText
    }

    fun onKindChange(newKind: Question.QuestionKind) {
        _kind.value = newKind
    }

    fun onToggleLevel() {
        _isTopicLevel.value = !_isTopicLevel.value
        if (_isTopicLevel.value) {
            _selectedClaim.value = null
        }
    }

    fun onClaimSelected(claim: Claim?) {
        _selectedClaim.value = claim
        if (claim != null) {
            _isTopicLevel.value = false
        }
    }

    fun saveQuestion(onSaved: () -> Unit) {
        if (_text.value.isBlank()) {
            _errorMessage.value = resourceProvider.getString(R.string.error_question_text_empty)
            return
        }

        // Determine final targetId: claim if selected, otherwise topic
        val finalTargetId = _selectedClaim.value?.id ?: initialTopicId ?: targetId
        if (finalTargetId == null) {
            _errorMessage.value = "No target selected for question"
            return
        }

        _errorMessage.value = null

        viewModelScope.launch {
            _isSaving.value = true
            val result = runCatching {
                val qId = _questionId.value
                val question = if (qId != null) {
                    // Update existing question
                    Timber.d("Updating existing question: $qId")
                    questionRepository.getQuestionById(qId)?.copy(
                        targetId = finalTargetId,
                        text = _text.value,
                        kind = _kind.value,
                        updatedAt = com.argumentor.app.data.model.getCurrentIsoTimestamp()
                    )
                } else {
                    // Create new question
                    Timber.d("Creating new question")
                    Question(
                        targetId = finalTargetId,
                        text = _text.value,
                        kind = _kind.value
                    )
                }

                question?.let {
                    questionRepository.insertQuestion(it)
                    if (qId != null) {
                        Timber.d("Question updated successfully: $qId")
                    } else {
                        Timber.d("Question created successfully: ${it.id}")
                    }
                }
            }

            result.onSuccess {
                onSaved()
            }.onFailure { e ->
                Timber.e(e, "Failed to save question")
                _errorMessage.value = resourceProvider.getString(
                    R.string.error_save_question,
                    e.message ?: resourceProvider.getString(R.string.error_unknown)
                )
            }

            _isSaving.value = false
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return _text.value != _initialText.value ||
                _kind.value != _initialKind.value ||
                _initialTargetId.value != null  // Has unsaved changes if editing
    }
}
