package com.argumentor.app.ui.screens.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.Question
import com.argumentor.app.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionCreateEditViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _kind = MutableStateFlow(Question.QuestionKind.CLARIFYING)
    val kind: StateFlow<Question.QuestionKind> = _kind.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var questionId: String? = null
    private var targetId: String? = null

    fun loadQuestion(questionId: String?, targetId: String?) {
        this.targetId = targetId
        if (questionId != null) {
            this.questionId = questionId
            viewModelScope.launch {
                questionRepository.getQuestionById(questionId)?.let { question ->
                    _text.value = question.text
                    _kind.value = question.kind
                }
            }
        }
    }

    fun onTextChange(newText: String) {
        _text.value = newText
    }

    fun onKindChange(newKind: Question.QuestionKind) {
        _kind.value = newKind
    }

    fun saveQuestion(onSaved: () -> Unit) {
        if (_text.value.isBlank() || targetId == null) return

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val question = if (questionId != null) {
                    // Update existing question
                    questionRepository.getQuestionById(questionId!!)?.copy(
                        text = _text.value,
                        kind = _kind.value
                    )
                } else {
                    // Create new question
                    Question(
                        targetId = targetId!!,
                        text = _text.value,
                        kind = _kind.value
                    )
                }

                question?.let {
                    questionRepository.insertQuestion(it)
                    onSaved()
                }
            } finally {
                _isSaving.value = false
            }
        }
    }
}
