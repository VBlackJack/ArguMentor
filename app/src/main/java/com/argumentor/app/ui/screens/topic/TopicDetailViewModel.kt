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

                // Build evidences map (for claims and rebuttals)
                val evidencesMap = mutableMapOf<String, List<Evidence>>()
                claims.forEach { claim ->
                    evidencesMap[claim.id] = evidenceRepository.getEvidencesByClaimId(claim.id).first()
                }
                rebuttalsMap.values.flatten().forEach { rebuttal ->
                    evidencesMap[rebuttal.id] = evidenceRepository.getEvidencesByClaimId(rebuttal.id).first()
                }

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
