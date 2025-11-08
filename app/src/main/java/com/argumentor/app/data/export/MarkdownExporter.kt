package com.argumentor.app.data.export

import android.content.Context
import android.os.Environment
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Question
import com.argumentor.app.data.model.Rebuttal
import com.argumentor.app.data.model.Source
import com.argumentor.app.data.model.Topic
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exports topics and their arguments to Markdown format.
 */
class MarkdownExporter(private val context: Context) {

    /**
     * Export a single topic with all its data to Markdown
     */
    fun exportTopicToMarkdown(
        topic: Topic,
        claims: List<Claim>,
        rebuttals: Map<String, List<Rebuttal>>,
        evidence: Map<String, List<Evidence>>,
        questions: List<Question>,
        sources: Map<String, Source>
    ): Result<File> {
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

                        appendLine("### $stanceIcon ${claim.title}")
                        appendLine()
                        appendLine("**Position:** ${claim.stance.name}")
                        appendLine("**Force:** ${claim.strength.name}")
                        appendLine()
                        appendLine(claim.content)
                        appendLine()

                        // Evidence for this claim
                        evidence[claim.id]?.let { evidenceList ->
                            if (evidenceList.isNotEmpty()) {
                                appendLine("**Preuves:**")
                                evidenceList.forEach { ev ->
                                    appendLine("- **${ev.title}** (${ev.type.name})")
                                    appendLine("  ${ev.content}")
                                    if (ev.sourceId.isNotEmpty()) {
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
                                    appendLine("#### ↳ ${rebuttal.title}")
                                    appendLine()
                                    appendLine(rebuttal.content)
                                    appendLine()

                                    // Evidence for this rebuttal
                                    evidence[rebuttal.id]?.let { rebuttalEvidence ->
                                        if (rebuttalEvidence.isNotEmpty()) {
                                            appendLine("**Preuves:**")
                                            rebuttalEvidence.forEach { ev ->
                                                appendLine("- **${ev.title}** (${ev.type.name})")
                                                appendLine("  ${ev.content}")
                                                if (ev.sourceId.isNotEmpty()) {
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
                        appendLine("- ${question.content}")
                        if (question.context.isNotEmpty()) {
                            appendLine("  *${question.context}*")
                        }
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
                        appendLine("**Type:** ${source.type.name}")
                        if (source.author.isNotEmpty()) {
                            appendLine("**Auteur:** ${source.author}")
                        }
                        if (source.url.isNotEmpty()) {
                            appendLine("**URL:** [${source.url}](${source.url})")
                        }
                        if (source.publicationDate.isNotEmpty()) {
                            appendLine("**Date de publication:** ${source.publicationDate}")
                        }
                        appendLine()
                        appendLine(source.summary)
                        appendLine()
                        appendLine("**Fiabilité:** ${source.reliability.name}")
                        appendLine()
                    }
                }

                // Footer
                appendLine("---")
                appendLine()
                appendLine("*Généré par ArguMentor le ${formatDate(getCurrentIsoTimestamp())}*")
            }

            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "ArguMentor_${topic.title.take(20).replace(" ", "_")}_$timestamp.md"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            file.writeText(markdown)

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export multiple topics to a single Markdown file
     */
    fun exportMultipleTopicsToMarkdown(
        topics: List<Topic>,
        claimsMap: Map<String, List<Claim>>
    ): Result<File> {
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
                            appendLine("- [${claim.stance.name}] ${claim.title}")
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

            // Save to file
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "ArguMentor_AllTopics_$timestamp.md"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            file.writeText(markdown)

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
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
