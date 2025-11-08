package com.argumentor.app.ui.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val rebuttalRepository: RebuttalRepository,
    private val evidenceRepository: EvidenceRepository,
    private val questionRepository: QuestionRepository,
    private val sourceRepository: SourceRepository
) : ViewModel() {

    private val _topicId = MutableStateFlow<String?>(null)

    private val _topic = MutableStateFlow<Topic?>(null)
    val topic: StateFlow<Topic?> = _topic.asStateFlow()

    private val _claims = MutableStateFlow<List<Claim>>(emptyList())
    val claims: StateFlow<List<Claim>> = _claims.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun loadTopic(topicId: String) {
        _topicId.value = topicId

        viewModelScope.launch {
            topicRepository.getTopicById(topicId).collect { topic ->
                _topic.value = topic
            }
        }

        viewModelScope.launch {
            // Load claims associated with this topic
            claimRepository.getAllClaims().collect { allClaims ->
                _claims.value = allClaims.filter { claim ->
                    claim.topics.contains(topicId)
                }
            }
        }

        viewModelScope.launch {
            questionRepository.getQuestionsByTargetId(topicId).collect { questions ->
                _questions.value = questions
            }
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun deleteTopic(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _topic.value?.let { topic ->
                topicRepository.deleteTopic(topic)
                onDeleted()
            }
        }
    }

    fun getClaimRebuttals(claimId: String): Flow<List<Rebuttal>> {
        return rebuttalRepository.getRebuttalsByClaimId(claimId)
    }

    fun getClaimEvidences(claimId: String): Flow<List<Evidence>> {
        return evidenceRepository.getEvidencesByClaimId(claimId)
    }
}
