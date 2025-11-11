package com.argumentor.app.ui.screens.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Question
import com.argumentor.app.data.repository.ClaimRepository
import com.argumentor.app.data.repository.QuestionRepository
import com.argumentor.app.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionCreateEditViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val claimRepository: ClaimRepository,
    private val topicRepository: TopicRepository
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

    private var questionId: String? = null
    private var targetId: String? = null
    private var initialTopicId: String? = null

    fun loadQuestion(questionId: String?, targetId: String?) {
        this.targetId = targetId
        this.initialTopicId = targetId

        viewModelScope.launch {
            if (questionId != null) {
                // Editing existing question
                this@QuestionCreateEditViewModel.questionId = questionId
                questionRepository.getQuestionById(questionId)?.let { question ->
                    _text.value = question.text
                    _kind.value = question.kind

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
            _errorMessage.value = "Question text cannot be empty"
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
            try {
                val qId = questionId
                val question = if (qId != null) {
                    // Update existing question
                    questionRepository.getQuestionById(qId)?.copy(
                        targetId = finalTargetId,
                        text = _text.value,
                        kind = _kind.value,
                        updatedAt = com.argumentor.app.data.model.getCurrentIsoTimestamp()
                    )
                } else {
                    // Create new question
                    Question(
                        targetId = finalTargetId,
                        text = _text.value,
                        kind = _kind.value
                    )
                }

                question?.let {
                    questionRepository.insertQuestion(it)
                    onSaved()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save question: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
