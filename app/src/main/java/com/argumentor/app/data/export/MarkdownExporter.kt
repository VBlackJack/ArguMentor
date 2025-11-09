package com.argumentor.app.data.export

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Question
import com.argumentor.app.data.model.Rebuttal
import com.argumentor.app.data.model.Source
import com.argumentor.app.data.model.Topic
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exports topics and their arguments to Markdown format.
 */
class MarkdownExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Export a single topic with all its data to Markdown via OutputStream (for SAF).
     * Use this method with Storage Access Framework (CreateDocument).
     */
    fun exportTopicToMarkdown(
        topic: Topic,
        claims: List<Claim>,
        rebuttals: Map<String, List<Rebuttal>>,
        evidence: Map<String, List<Evidence>>,
        questions: List<Question>,
        sources: Map<String, Source>,
        outputStream: OutputStream
    ): Result<Unit> {
        return try {
            val markdown = buildString {
                // Title
                appendLine("# ${topic.title}")
                appendLine()

                // Metadata
                appendLine("**Posture:** ${topic.posture.name.replace("_", " ")}")
                appendLine()
                appendLine("**Créé le:** ${formatDate(topic.createdAt)}")
                appendLine("**Mis à jour le:** ${formatDate(topic.updatedAt)}")
                appendLine()

                // Tags
                if (topic.tags.isNotEmpty()) {
                    appendLine("**Tags:** ${topic.tags.joinToString(", ") { "`$it`" }}")
                    appendLine()
                }

                // Summary
                appendLine("## Résumé")
                appendLine()
                appendLine(topic.summary)
                appendLine()

                // Claims
                if (claims.isNotEmpty()) {
                    appendLine("## Arguments")
                    appendLine()

                    claims.sortedBy { it.stance }.forEach { claim ->
                        val stanceIcon = when (claim.stance.name) {
                            "PRO" -> "✅"
                            "CON" -> "❌"
                            else -> "🔵"
                        }

                        appendLine("### $stanceIcon ${claim.text.take(50)}...")
                        appendLine()
                        appendLine("**Position:** ${claim.stance.name}")
                        appendLine("**Force:** ${claim.strength.name}")
                        appendLine()
                        appendLine(claim.text)
                        appendLine()

                        // Evidence for this claim
                        evidence[claim.id]?.let { evidenceList ->
                            if (evidenceList.isNotEmpty()) {
                                appendLine("**Preuves:**")
                                evidenceList.forEach { ev ->
                                    appendLine("- (${ev.type.name})")
                                    appendLine("  ${ev.content}")
                                    if (ev.sourceId != null && ev.sourceId.isNotEmpty()) {
                                        sources[ev.sourceId]?.let { source ->
                                            appendLine("  *Source: ${source.title}*")
                                        }
                                    }
                                }
                                appendLine()
                            }
                        }

                        // Rebuttals for this claim
                        rebuttals[claim.id]?.let { rebuttalList ->
                            if (rebuttalList.isNotEmpty()) {
                                appendLine("**Contre-arguments:**")
                                appendLine()
                                rebuttalList.forEach { rebuttal ->
                                    appendLine("#### ↳ ${rebuttal.text.take(50)}...")
                                    appendLine()
                                    appendLine(rebuttal.text)
                                    appendLine()

                                    // Evidence for this rebuttal
                                    evidence[rebuttal.id]?.let { rebuttalEvidence ->
                                        if (rebuttalEvidence.isNotEmpty()) {
                                            appendLine("**Preuves:**")
                                            rebuttalEvidence.forEach { ev ->
                                                appendLine("- (${ev.type.name})")
                                                appendLine("  ${ev.content}")
                                                if (ev.sourceId != null && ev.sourceId.isNotEmpty()) {
                                                    sources[ev.sourceId]?.let { source ->
                                                        appendLine("  *Source: ${source.title}*")
                                                    }
                                                }
                                            }
                                            appendLine()
                                        }
                                    }
                                }
                            }
                        }

                        appendLine("---")
                        appendLine()
                    }
                }

                // Questions
                if (questions.isNotEmpty()) {
                    appendLine("## Questions en suspens")
                    appendLine()
                    questions.forEach { question ->
                        appendLine("- ${question.text}")
                    }
                    appendLine()
                }

                // Sources
                val allSources = sources.values.toList()
                if (allSources.isNotEmpty()) {
                    appendLine("## Sources")
                    appendLine()
                    allSources.forEach { source ->
                        appendLine("### ${source.title}")
                        appendLine()
                        source.citation?.let {
                            appendLine("**Citation:** $it")
                        }
                        source.url?.let {
                            if (it.isNotEmpty()) {
                                appendLine("**URL:** [$it]($it)")
                            }
                        }
                        source.date?.let {
                            if (it.isNotEmpty()) {
                                appendLine("**Date:** $it")
                            }
                        }
                        appendLine()
                        source.notes?.let {
                            if (it.isNotEmpty()) {
                                appendLine(it)
                                appendLine()
                            }
                        }
                        source.reliabilityScore?.let {
                            appendLine("**Fiabilité:** ${(it * 100).toInt()}%")
                        }
                        appendLine()
                    }
                }

                // Footer
                appendLine("---")
                appendLine()
                appendLine("*Généré par ArguMentor le ${formatDate(getCurrentIsoTimestamp())}*")
            }

            // Write to OutputStream (SAF-compatible)
            outputStream.write(markdown.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                outputStream.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }

    /**
     * Export multiple topics to a single Markdown file via OutputStream (for SAF).
     * Use this method with Storage Access Framework (CreateDocument).
     */
    fun exportMultipleTopicsToMarkdown(
        topics: List<Topic>,
        claimsMap: Map<String, List<Claim>>,
        outputStream: OutputStream
    ): Result<Unit> {
        return try {
            val markdown = buildString {
                appendLine("# Mes Topics ArguMentor")
                appendLine()
                appendLine("*Exporté le ${formatDate(getCurrentIsoTimestamp())}*")
                appendLine()
                appendLine("---")
                appendLine()

                topics.forEach { topic ->
                    appendLine("## ${topic.title}")
                    appendLine()
                    appendLine("**Posture:** ${topic.posture.name.replace("_", " ")}")
                    appendLine()
                    appendLine(topic.summary)
                    appendLine()

                    claimsMap[topic.id]?.let { claims ->
                        appendLine("**Arguments:** ${claims.size}")
                        claims.take(3).forEach { claim ->
                            appendLine("- [${claim.stance.name}] ${claim.text.take(50)}...")
                        }
                        if (claims.size > 3) {
                            appendLine("- *... et ${claims.size - 3} autres*")
                        }
                    }

                    appendLine()
                    appendLine("---")
                    appendLine()
                }

                appendLine("*Généré par ArguMentor*")
            }

            // Write to OutputStream (SAF-compatible)
            outputStream.write(markdown.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                outputStream.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }

    private fun formatDate(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRENCH)
            val date = inputFormat.parse(isoDate)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun getCurrentIsoTimestamp(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }
}
