package com.argumentor.app.ui.screens.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.R
import com.argumentor.app.data.export.MarkdownExporter
import com.argumentor.app.data.export.PdfExporter
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import com.argumentor.app.util.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
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
    private val markdownExporter: MarkdownExporter,
    private val resourceProvider: ResourceProvider
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

    /**
     * Loads topic data with all related entities.
     *
     * BUG FIX (BUG-002): Race condition fixed by loading all data in a single
     * coordinated flow. All entities are loaded synchronously to ensure proper
     * data dependencies and prevent partial state updates.
     */
    fun loadTopic(topicId: String) {
        _topicId.value = topicId

        // RACE CONDITION FIX: Load all data in a single coordinated flow
        viewModelScope.launch {
            combine(
                topicRepository.getTopicById(topicId),
                claimRepository.getAllClaims(),
                sourceRepository.getAllSources(),
                questionRepository.getQuestionsByTargetId(topicId)
            ) { topic, allClaims, allSources, topicQuestions ->
                // Filter claims for this topic
                val filteredClaims = allClaims.filter { claim -> claim.topics.contains(topicId) }

                // Return all data together
                TopicData(
                    topic = topic,
                    claims = filteredClaims,
                    sources = allSources,
                    topicQuestions = topicQuestions,
                    claimIds = filteredClaims.map { it.id }
                )
            }.flatMapLatest { data ->
                // Load questions for all claims
                if (data.claimIds.isEmpty()) {
                    flowOf(data to data.topicQuestions)
                } else {
                    val claimQuestionsFlows = data.claimIds.map { claimId ->
                        questionRepository.getQuestionsByTargetId(claimId)
                    }
                    combine(claimQuestionsFlows) { claimQuestionsArrays ->
                        val allQuestions = mutableListOf<Question>()
                        allQuestions.addAll(data.topicQuestions)
                        claimQuestionsArrays.forEach { claimQuestions ->
                            allQuestions.addAll(claimQuestions)
                        }
                        data to allQuestions.distinctBy { it.id }
                    }
                }
            }.collect { (data, allQuestions) ->
                // Update all state atomically at once to prevent inconsistent UI state
                _topic.value = data.topic
                _claims.value = data.claims
                _sources.value = data.sources
                _questions.value = allQuestions
            }
        }
    }

    /**
     * Internal data class to hold coordinated topic data during loading.
     */
    private data class TopicData(
        val topic: Topic?,
        val claims: List<Claim>,
        val sources: List<Source>,
        val topicQuestions: List<Question>,
        val claimIds: List<String>
    )

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    /**
     * BUG-006: Added delay to ensure cascade deletions complete before callback.
     * Without this, the UI might navigate away before related claims are fully deleted.
     */
    fun deleteTopic(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _topic.value?.let { topic ->
                try {
                    topicRepository.deleteTopic(topic)
                    // Wait briefly for cascade deletions to complete
                    kotlinx.coroutines.delay(150)
                    onDeleted()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to delete topic")
                    // Optionally show error to user
                }
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

                // Load all rebuttals at once instead of N+1 queries
                val allRebuttals = rebuttalRepository.getAllRebuttals().first()
                val rebuttalsMap = claims.associate { claim ->
                    claim.id to allRebuttals.filter { it.claimId == claim.id }
                }

                // Export to PDF
                pdfExporter.exportTopicToPdf(topic, claims, rebuttalsMap, outputStream).getOrThrow()
                Timber.d("PDF export successful for topic: $topicId")
            }.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                Timber.e(error, "Failed to export topic to PDF")
                onResult(false, error.message ?: resourceProvider.getString(R.string.error_export_pdf))
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

                // Load all data at once instead of N+1 queries
                val allRebuttals = rebuttalRepository.getAllRebuttals().first()
                val allEvidences = evidenceRepository.getAllEvidences().first()
                val allSources = sourceRepository.getAllSources().first()

                // Build rebuttals map
                val rebuttalsMap = claims.associate { claim ->
                    claim.id to allRebuttals.filter { it.claimId == claim.id }
                }

                // Build evidences map (for claims only)
                val evidencesMap = claims.associate { claim ->
                    claim.id to allEvidences.filter { it.claimId == claim.id }
                }
                // Note: Evidence is linked to claims, not rebuttals (as per EvidenceDao comment)

                // Build sources map
                val relevantEvidences = evidencesMap.values.flatten()
                val sourceIds = relevantEvidences.mapNotNull { it.sourceId }.filter { it.isNotEmpty() }.distinct()
                val sourcesMap = allSources.filter { it.id in sourceIds }.associateBy { it.id }

                // Export to Markdown
                markdownExporter.exportTopicToMarkdown(
                    topic, claims, rebuttalsMap, evidencesMap, questions, sourcesMap, outputStream
                ).getOrThrow()
                Timber.d("Markdown export successful for topic: $topicId")
            }.onSuccess {
                onResult(true, null)
            }.onFailure { error ->
                Timber.e(error, "Failed to export topic to Markdown")
                onResult(false, error.message ?: resourceProvider.getString(R.string.error_export_markdown))
            }
        }
    }
}
