package com.argumentor.app.data.repository

import android.content.Context
import com.argumentor.app.data.export.MarkdownExporter
import com.argumentor.app.data.export.PdfExporter
import com.argumentor.app.data.local.dao.*
import com.argumentor.app.data.model.Topic
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing advanced export operations (PDF, Markdown).
 */
@Singleton
class ExportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val topicDao: TopicDao,
    private val claimDao: ClaimDao,
    private val rebuttalDao: RebuttalDao,
    private val evidenceDao: EvidenceDao,
    private val questionDao: QuestionDao,
    private val sourceDao: SourceDao
) {

    private val pdfExporter = PdfExporter(context)
    private val markdownExporter = MarkdownExporter(context)

    /**
     * Export a single topic to PDF
     */
    suspend fun exportTopicToPdf(topicId: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val topic = topicDao.getTopicById(topicId) ?: return@withContext Result.failure(
                Exception("Topic not found")
            )

            val claims = claimDao.getClaimsForTopic(topicId)
            val rebuttals = mutableMapOf<String, List<com.argumentor.app.data.model.Rebuttal>>()

            claims.forEach { claim ->
                val claimRebuttals = rebuttalDao.getRebuttalsForClaim(claim.id)
                if (claimRebuttals.isNotEmpty()) {
                    rebuttals[claim.id] = claimRebuttals
                }
            }

            pdfExporter.exportTopicToPdf(topic, claims, rebuttals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export a single topic to Markdown
     */
    suspend fun exportTopicToMarkdown(topicId: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val topic = topicDao.getTopicById(topicId) ?: return@withContext Result.failure(
                Exception("Topic not found")
            )

            val claims = claimDao.getClaimsForTopic(topicId)
            val rebuttals = mutableMapOf<String, List<com.argumentor.app.data.model.Rebuttal>>()
            val evidence = mutableMapOf<String, List<com.argumentor.app.data.model.Evidence>>()
            val questions = questionDao.getQuestionsForTopic(topicId)
            val sources = mutableMapOf<String, com.argumentor.app.data.model.Source>()

            // Collect rebuttals for each claim
            claims.forEach { claim ->
                val claimRebuttals = rebuttalDao.getRebuttalsForClaim(claim.id)
                if (claimRebuttals.isNotEmpty()) {
                    rebuttals[claim.id] = claimRebuttals
                }

                // Collect evidence for each claim
                val claimEvidence = evidenceDao.getEvidenceForClaim(claim.id)
                if (claimEvidence.isNotEmpty()) {
                    evidence[claim.id] = claimEvidence

                    // Collect sources
                    claimEvidence.forEach { ev ->
                        if (ev.sourceId.isNotEmpty()) {
                            sourceDao.getSourceById(ev.sourceId)?.let { source ->
                                sources[source.id] = source
                            }
                        }
                    }
                }
            }

            // Collect evidence for rebuttals
            rebuttals.values.flatten().forEach { rebuttal ->
                val rebuttalEvidence = evidenceDao.getEvidenceForRebuttal(rebuttal.id)
                if (rebuttalEvidence.isNotEmpty()) {
                    evidence[rebuttal.id] = rebuttalEvidence

                    // Collect sources
                    rebuttalEvidence.forEach { ev ->
                        if (ev.sourceId.isNotEmpty()) {
                            sourceDao.getSourceById(ev.sourceId)?.let { source ->
                                sources[source.id] = source
                            }
                        }
                    }
                }
            }

            markdownExporter.exportTopicToMarkdown(topic, claims, rebuttals, evidence, questions, sources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export all topics to a single Markdown file
     */
    suspend fun exportAllTopicsToMarkdown(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val topics = topicDao.getAllTopicsSync()
            val claimsMap = mutableMapOf<String, List<com.argumentor.app.data.model.Claim>>()

            topics.forEach { topic ->
                claimsMap[topic.id] = claimDao.getClaimsForTopic(topic.id)
            }

            markdownExporter.exportMultipleTopicsToMarkdown(topics, claimsMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get simple text representation of a topic for sharing
     */
    suspend fun getTopicAsText(topicId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val topic = topicDao.getTopicById(topicId) ?: return@withContext Result.failure(
                Exception("Topic not found")
            )

            val claims = claimDao.getClaimsForTopic(topicId)

            val text = buildString {
                appendLine(topic.title)
                appendLine("=" .repeat(topic.title.length))
                appendLine()
                appendLine(topic.summary)
                appendLine()
                appendLine("Arguments:")
                claims.forEach { claim ->
                    appendLine("• [${claim.stance.name}] ${claim.title}")
                    appendLine("  ${claim.content}")
                    appendLine()
                }
            }

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
