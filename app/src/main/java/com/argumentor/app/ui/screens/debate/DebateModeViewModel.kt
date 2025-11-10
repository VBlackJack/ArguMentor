package com.argumentor.app.ui.screens.debate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebateCard(
    val claim: Claim,
    val rebuttals: List<Rebuttal>,
    val evidences: List<Evidence>,
    val questions: List<Question>
)

@HiltViewModel
class DebateModeViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val rebuttalRepository: RebuttalRepository,
    private val evidenceRepository: EvidenceRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _topic = MutableStateFlow<Topic?>(null)
    val topic: StateFlow<Topic?> = _topic.asStateFlow()

    private val _debateCards = MutableStateFlow<List<DebateCard>>(emptyList())
    val debateCards: StateFlow<List<DebateCard>> = _debateCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    companion object {
        private const val MAX_REBUTTALS_PER_CARD = 2
        private const val MAX_EVIDENCES_PER_CARD = 2
        private const val MAX_QUESTIONS_PER_CARD = 1
    }

    fun loadTopic(topicId: String) {
        viewModelScope.launch {
            topicRepository.getTopicById(topicId).collect { topic ->
                _topic.value = topic
            }
        }

        viewModelScope.launch {
            claimRepository.getAllClaims().collect { allClaims ->
                val topicClaims = allClaims.filter { it.topics.contains(topicId) }

                // Load related data for each claim
                val cards = topicClaims.map { claim ->
                    val rebuttals = rebuttalRepository.getRebuttalsByClaimId(claim.id).first()
                    val evidences = evidenceRepository.getEvidencesByClaimId(claim.id).first()
                    val questions = questionRepository.getQuestionsByTargetId(claim.id).first()

                    DebateCard(
                        claim = claim,
                        rebuttals = rebuttals.take(MAX_REBUTTALS_PER_CARD),
                        evidences = evidences.take(MAX_EVIDENCES_PER_CARD),
                        questions = questions.take(MAX_QUESTIONS_PER_CARD)
                    )
                }

                _debateCards.value = cards.shuffled() // Shuffle for practice
            }
        }
    }

    fun nextCard() {
        if (_currentCardIndex.value < _debateCards.value.size - 1) {
            _currentCardIndex.value += 1
            _isCardFlipped.value = false
        }
    }

    fun previousCard() {
        if (_currentCardIndex.value > 0) {
            _currentCardIndex.value -= 1
            _isCardFlipped.value = false
        }
    }

    fun goToCard(index: Int) {
        if (index in 0 until _debateCards.value.size) {
            _currentCardIndex.value = index
            _isCardFlipped.value = false
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun resetProgress() {
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
        _debateCards.value = _debateCards.value.shuffled()
    }
}
