package com.argumentor.app.ui.screens.debate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class DebateCard(
    val claim: Claim,
    val rebuttals: List<Rebuttal>,
    val evidences: List<Evidence>,
    val questions: List<Question>,
    val fallacies: Map<String, com.argumentor.app.data.model.Fallacy> = emptyMap()
)

@HiltViewModel
class DebateModeViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val rebuttalRepository: RebuttalRepository,
    private val evidenceRepository: EvidenceRepository,
    private val questionRepository: QuestionRepository,
    private val fallacyRepository: com.argumentor.app.data.repository.FallacyRepository
) : ViewModel() {

    private val _topic = MutableStateFlow<Topic?>(null)
    val topic: StateFlow<Topic?> = _topic.asStateFlow()

    private val _debateCards = MutableStateFlow<List<DebateCard>>(emptyList())
    val debateCards: StateFlow<List<DebateCard>> = _debateCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    // Gamification states
    private val _cardsReviewed = MutableStateFlow<Set<String>>(emptySet())
    val cardsReviewed: StateFlow<Set<String>> = _cardsReviewed.asStateFlow()

    private val _sessionScore = MutableStateFlow(0)
    val sessionScore: StateFlow<Int> = _sessionScore.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    companion object {
        private const val MAX_REBUTTALS_PER_CARD = 2
        private const val MAX_EVIDENCES_PER_CARD = 2
        private const val MAX_QUESTIONS_PER_CARD = 1
        private const val POINTS_PER_CARD = 10
        private const val BONUS_STREAK_MULTIPLIER = 2
    }

    /**
     * Loads topic and all related debate cards.
     * MEMORY-003 FIX: Combines flows to avoid race conditions and memory leaks.
     */
    fun loadTopic(topicId: String) {
        viewModelScope.launch {
            // MEMORY-003 FIX: Combine topic and claims flows to avoid race conditions
            combine(
                topicRepository.getTopicById(topicId),
                claimRepository.getAllClaims()
            ) { topic, allClaims ->
                _topic.value = topic
                Pair(topic, allClaims.filter { it.topics.contains(topicId) })
            }
            .collectLatest { (topic, topicClaims) ->
                if (topic == null) {
                    Timber.w("Topic not found: $topicId")
                    return@collectLatest
                }

                // Load all data at once instead of N+1 queries (optimization)
                Timber.d("Loading debate cards for ${topicClaims.size} claims")

                val allRebuttals = rebuttalRepository.getAllRebuttals().first()
                val allEvidences = evidenceRepository.getAllEvidences().first()
                val allQuestions = questionRepository.getQuestionsByTargetId(topicId).first()

                // Build maps for quick lookup
                val rebuttalsMap = allRebuttals.groupBy { it.claimId }
                val evidencesMap = allEvidences.groupBy { it.claimId }

                // Questions can target claims, so filter by targetId
                val questionsMap = allQuestions.groupBy { it.targetId }

                // Collect all unique fallacy IDs from claims and rebuttals
                val allFallacyIds = mutableSetOf<String>()
                topicClaims.forEach { allFallacyIds.addAll(it.fallacyIds) }
                allRebuttals.forEach { allFallacyIds.addAll(it.fallacyIds) }

                // PERF-001 FIX: Load all fallacies at once using optimized bulk query
                val allFallacies = fallacyRepository.getFallaciesByIds(allFallacyIds.toList())
                val fallaciesMap = allFallacies.associateBy { it.id }

                // Build cards using pre-loaded data
                val cards = topicClaims.map { claim ->
                    DebateCard(
                        claim = claim,
                        rebuttals = (rebuttalsMap[claim.id] ?: emptyList()).take(MAX_REBUTTALS_PER_CARD),
                        evidences = (evidencesMap[claim.id] ?: emptyList()).take(MAX_EVIDENCES_PER_CARD),
                        questions = (questionsMap[claim.id] ?: emptyList()).take(MAX_QUESTIONS_PER_CARD),
                        fallacies = fallaciesMap
                    )
                }

                Timber.d("Loaded ${cards.size} debate cards successfully")
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

        // Mark card as reviewed and update score when flipping to answer side
        if (_isCardFlipped.value && _debateCards.value.isNotEmpty()) {
            val currentCard = _debateCards.value[_currentCardIndex.value]
            if (!_cardsReviewed.value.contains(currentCard.claim.id)) {
                _cardsReviewed.value = _cardsReviewed.value + currentCard.claim.id

                // Calculate score with streak bonus
                val basePoints = POINTS_PER_CARD
                val streakBonus = if (_streak.value >= 3) BONUS_STREAK_MULTIPLIER else 1
                _sessionScore.value += basePoints * streakBonus
                _streak.value += 1
            }
        }
    }

    fun markCardDifficult() {
        // Reset streak if card is difficult
        _streak.value = 0
    }

    fun resetProgress() {
        _currentCardIndex.value = 0
        _isCardFlipped.value = false
        _cardsReviewed.value = emptySet()
        _sessionScore.value = 0
        _streak.value = 0
        _debateCards.value = _debateCards.value.shuffled()
    }
}
