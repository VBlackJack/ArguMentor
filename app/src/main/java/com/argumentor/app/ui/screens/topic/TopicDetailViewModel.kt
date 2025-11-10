package com.argumentor.app.ui.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.export.MarkdownExporter
import com.argumentor.app.data.export.PdfExporter
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class TopicDetailViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val rebuttalRepository: RebuttalRepository,
    private val evidenceRepository: EvidenceRepository,
    private val questionRepository: QuestionRepository,
    private val sourceRepository: SourceRepository,
    private val pdfExporter: PdfExporter,
    private val markdownExporter: MarkdownExporter
) : ViewModel() {

    private val _topicId = MutableStateFlow<String?>(null)

    private val _topic = MutableStateFlow<Topic?>(null)
    val topic: StateFlow<Topic?> = _topic.asStateFlow()

    private val _claims = MutableStateFlow<List<Claim>>(emptyList())
    val claims: StateFlow<List<Claim>> = _claims.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _sources = MutableStateFlow<List<Source>>(emptyList())
    val sources: StateFlow<List<Source>> = _sources.asStateFlow()

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
            // Combine questions for the topic and questions for all claims in the topic
            combine(
                questionRepository.getQuestionsByTargetId(topicId),
                _claims
            ) { topicQuestions, claims ->
                val claimIds = claims.map { it.id }
                val allQuestions = mutableListOf<Question>()
                allQuestions.addAll(topicQuestions)

                // Add questions from all claims
                claimIds.forEach { claimId ->
                    val claimQuestions = questionRepository.getQuestionsByTargetId(claimId).first()
                    allQuestions.addAll(claimQuestions)
                }

                allQuestions.distinctBy { it.id }
            }.collect { allQuestions ->
                _questions.value = allQuestions
            }
        }

        viewModelScope.launch {
            // Load sources - collect all sources from evidences
            _claims.collect { claims ->
                val claimIds = claims.map { it.id }
                val allEvidences = claimIds.flatMap { claimId ->
                    evidenceRepository.getEvidencesByClaimId(claimId).first()
                }
                val sourceIds = allEvidences.mapNotNull { it.sourceId }.filter { it.isNotEmpty() }.distinct()
                val sources = sourceIds.mapNotNull { sourceId ->
                    sourceRepository.getSourceById(sourceId).first()
                }
                _sources.value = sources
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

    fun deleteClaim(claim: Claim, onDeleted: () -> Unit) {
        viewModelScope.launch {
            claimRepository.deleteClaim(claim)
            onDeleted()
        }
    }

    fun restoreClaim(claim: Claim) {
        viewModelScope.launch {
            claimRepository.insertClaim(claim)
        }
    }

    fun deleteQuestion(question: Question, onDeleted: () -> Unit) {
        viewModelScope.launch {
            questionRepository.deleteQuestion(question)
            onDeleted()
        }
    }

    fun restoreQuestion(question: Question) {
        viewModelScope.launch {
            questionRepository.insertQuestion(question)
        }
    }

    fun deleteEvidence(evidence: Evidence, onDeleted: () -> Unit) {
        viewModelScope.launch {
            evidenceRepository.deleteEvidence(evidence)
            onDeleted()
        }
    }

    fun restoreEvidence(evidence: Evidence) {
        viewModelScope.launch {
            evidenceRepository.insertEvidence(evidence)
        }
    }

    fun deleteSource(source: Source, onDeleted: () -> Unit) {
        viewModelScope.launch {
            sourceRepository.deleteSource(source)
            onDeleted()
        }
    }

    fun restoreSource(source: Source) {
        viewModelScope.launch {
            sourceRepository.insertSource(source)
        }
    }

    fun getClaimRebuttals(claimId: String): Flow<List<Rebuttal>> {
        return rebuttalRepository.getRebuttalsByClaimId(claimId)
    }

    fun getClaimEvidences(claimId: String): Flow<List<Evidence>> {
        return evidenceRepository.getEvidencesByClaimId(claimId)
    }

    /**
     * Export topic to PDF format via SAF OutputStream.
     * @param topicId The topic to export
     * @param outputStream OutputStream from SAF CreateDocument
     * @param onResult Callback with (success: Boolean, errorMessage: String?)
     */
    fun exportTopicToPdf(topicId: String, outputStream: OutputStream, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                // Gather all data for export
                val topic = _topic.value ?: error("Topic not found")
                val claims = _claims.value

                // Build rebuttals map
                val rebuttalsMap = claims.associate { claim ->
                    claim.id to rebuttalRepository.getRebuttalsByClaimId(claim.id).first()
                }

                // Export to PDF
                pdfExporter.exportTopicToPdf(topic, claims, rebuttalsMap, outputStream).getOrThrow()
            }.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                onResult(false, error.message ?: "Erreur lors de l'export PDF")
            }
        }
    }

    /**
     * Export topic to Markdown format via SAF OutputStream.
     * @param topicId The topic to export
     * @param outputStream OutputStream from SAF CreateDocument
     * @param onResult Callback with (success: Boolean, errorMessage: String?)
     */
    fun exportTopicToMarkdown(topicId: String, outputStream: OutputStream, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            runCatching {
                // Gather all data for export
                val topic = _topic.value ?: error("Topic not found")
                val claims = _claims.value
                val questions = _questions.value

                // Build rebuttals map
                val rebuttalsMap = claims.associate { claim ->
                    claim.id to rebuttalRepository.getRebuttalsByClaimId(claim.id).first()
                }

                // Build evidences map (for claims only)
                val evidencesMap = mutableMapOf<String, List<Evidence>>()
                claims.forEach { claim ->
                    evidencesMap[claim.id] = evidenceRepository.getEvidencesByClaimId(claim.id).first()
                }
                // Note: Evidence is linked to claims, not rebuttals (as per EvidenceDao comment)

                // Build sources map
                val allEvidences = evidencesMap.values.flatten()
                val sourceIds = allEvidences.mapNotNull { it.sourceId }.filter { it.isNotEmpty() }.distinct()
                val sourcesMap = sourceIds.mapNotNull { sourceId ->
                    sourceRepository.getSourceById(sourceId).first()?.let { sourceId to it }
                }.toMap()

                // Export to Markdown
                markdownExporter.exportTopicToMarkdown(
                    topic, claims, rebuttalsMap, evidencesMap, questions, sourcesMap, outputStream
                ).getOrThrow()
            }.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                onResult(false, error.message ?: "Erreur lors de l'export Markdown")
            }
        }
    }
}
