package com.argumentor.app.data.export

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.argumentor.app.R
import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Evidence
import com.argumentor.app.data.model.Question
import com.argumentor.app.data.model.Rebuttal
import com.argumentor.app.data.model.Source
import com.argumentor.app.data.model.Topic
import java.io.OutputStream
import java.util.*

/**
 * Exports topics and their arguments to Markdown format.
 *
 * INTERNATIONALIZATION: All labels now use string resources for proper multi-language support.
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

                // Metadata - BUGFIX: Use string resources instead of hardcoded French
                appendLine("**${context.getString(R.string.export_posture_label)}** ${topic.posture.name.replace("_", " ")}")
                appendLine()
                appendLine("**${context.getString(R.string.export_created_label)}** ${formatDate(topic.createdAt)}")
                appendLine("**${context.getString(R.string.export_updated_label)}** ${formatDate(topic.updatedAt)}")
                appendLine()

                // Tags - BUGFIX: Use string resources
                if (topic.tags.isNotEmpty()) {
                    appendLine("**${context.getString(R.string.export_tags_label)}** ${topic.tags.joinToString(", ") { "`$it`" }}")
                    appendLine()
                }

                // Summary - BUGFIX: Use string resources
                appendLine("## ${context.getString(R.string.export_summary_label)}")
                appendLine()
                appendLine(topic.summary)
                appendLine()

                // Claims - BUGFIX: Use string resources
                if (claims.isNotEmpty()) {
                    appendLine("## ${context.getString(R.string.export_arguments_label)}")
                    appendLine()

                    claims.sortedBy { it.stance }.forEach { claim ->
                        val stanceIcon = when (claim.stance.name) {
                            "PRO" -> "✅"
                            "CON" -> "❌"
                            else -> "🔵"
                        }

                        appendLine("### $stanceIcon ${claim.text.take(50)}...")
                        appendLine()
                        appendLine("**${context.getString(R.string.export_position_label)}** ${claim.stance.name}")
                        appendLine("**${context.getString(R.string.export_strength_label)}** ${claim.strength.name}")
                        appendLine()
                        appendLine(claim.text)
                        appendLine()

                        // Evidence for this claim - BUGFIX: Use string resources
                        evidence[claim.id]?.let { evidenceList ->
                            if (evidenceList.isNotEmpty()) {
                                appendLine("**${context.getString(R.string.export_evidence_label)}**")
                                evidenceList.forEach { ev ->
                                    appendLine("- (${ev.type.name})")
                                    appendLine("  ${ev.content}")
                                    if (ev.sourceId != null && ev.sourceId.isNotEmpty()) {
                                        sources[ev.sourceId]?.let { source ->
                                            appendLine("  *${context.getString(R.string.export_source_label)} ${source.title}*")
                                        }
                                    }
                                }
                                appendLine()
                            }
                        }

                        // Rebuttals for this claim - BUGFIX: Use string resources
                        rebuttals[claim.id]?.let { rebuttalList ->
                            if (rebuttalList.isNotEmpty()) {
                                appendLine("**${context.getString(R.string.export_rebuttals_label)}**")
                                appendLine()
                                rebuttalList.forEach { rebuttal ->
                                    appendLine("#### ↳ ${rebuttal.text.take(50)}...")
                                    appendLine()
                                    appendLine(rebuttal.text)
                                    appendLine()

                                    // BUGFIX REMOVED: Evidence is linked to Claims, not Rebuttals
                                    // The previous code tried to find evidence[rebuttal.id] which is incorrect
                                    // because the evidence Map is keyed by claimId, not rebuttalId.
                                    // According to the schema (EvidenceDao lines 22-27), Evidence entities
                                    // have a claimId foreign key, not a rebuttalId.
                                    // If rebuttals need their own evidence in the future, the schema
                                    // would need to be updated first.
                                }
                            }
                        }

                        appendLine("---")
                        appendLine()
                    }
                }

                // Questions - BUGFIX: Use string resources
                if (questions.isNotEmpty()) {
                    appendLine("## ${context.getString(R.string.export_questions_label)}")
                    appendLine()
                    questions.forEach { question ->
                        appendLine("- ${question.text}")
                    }
                    appendLine()
                }

                // Sources - BUGFIX: Use string resources
                val allSources = sources.values.toList()
                if (allSources.isNotEmpty()) {
                    appendLine("## ${context.getString(R.string.export_sources_label)}")
                    appendLine()
                    allSources.forEach { source ->
                        appendLine("### ${source.title}")
                        appendLine()
                        source.citation?.let {
                            appendLine("**${context.getString(R.string.export_citation_label)}** $it")
                        }
                        source.url?.let {
                            if (it.isNotEmpty()) {
                                appendLine("**${context.getString(R.string.export_url_label)}** [$it]($it)")
                            }
                        }
                        source.date?.let {
                            if (it.isNotEmpty()) {
                                appendLine("**${context.getString(R.string.export_date_label)}** $it")
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
                            appendLine("**${context.getString(R.string.export_reliability_label)}** ${(it * 100).toInt()}%")
                        }
                        appendLine()
                    }
                }

                // Footer - BUGFIX: Use string resources
                appendLine("---")
                appendLine()
                appendLine("*${context.getString(R.string.export_generated_by, formatDate(getCurrentIsoTimestamp()))}*")
            }

            // BUG-007: Write to OutputStream (SAF-compatible)
            // Note: We don't close the stream here as it's passed by the caller
            // who is responsible for its lifecycle management
            outputStream.write(markdown.toByteArray(Charsets.UTF_8))
            outputStream.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
                // BUGFIX: Use string resources instead of hardcoded French
                appendLine("# ${context.getString(R.string.export_multiple_topics_title)}")
                appendLine()
                appendLine("*${context.getString(R.string.export_exported_label, formatDate(getCurrentIsoTimestamp()))}*")
                appendLine()
                appendLine("---")
                appendLine()

                topics.forEach { topic ->
                    appendLine("## ${topic.title}")
                    appendLine()
                    appendLine("**${context.getString(R.string.export_posture_label)}** ${topic.posture.name.replace("_", " ")}")
                    appendLine()
                    appendLine(topic.summary)
                    appendLine()

                    claimsMap[topic.id]?.let { claims ->
                        appendLine("**${context.getString(R.string.export_arguments_label)}** ${claims.size}")
                        claims.take(3).forEach { claim ->
                            appendLine("- [${claim.stance.name}] ${claim.text.take(50)}...")
                        }
                        if (claims.size > 3) {
                            appendLine("- *${context.getString(R.string.export_and_more, claims.size - 3)}*")
                        }
                    }

                    appendLine()
                    appendLine("---")
                    appendLine()
                }

                appendLine("*${context.getString(R.string.export_generated_by, formatDate(getCurrentIsoTimestamp()))}*")
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
     * BUG-004 FIX: Use java.time.Instant for proper ISO 8601 parsing
     * SimpleDateFormat doesn't handle 'Z' timezone properly in ISO 8601 format
     * This fix properly parses timestamps like "2024-01-01T12:00:00Z"
     *
     * LOCALE FIX: Use system default locale instead of hardcoded Locale.FRENCH
     * for proper internationalization support
     */
    private fun formatDate(isoDate: String): String {
        return try {
            val instant = java.time.Instant.parse(isoDate)
            val zonedDateTime = instant.atZone(java.time.ZoneId.systemDefault())
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm", Locale.getDefault())
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            // Fallback to original string if parsing fails
            isoDate
        }
    }

    /**
     * BUG-004 FIX: Use java.time.Instant for proper ISO 8601 timestamp generation
     * This ensures timestamps are in proper ISO 8601 format with UTC timezone
     */
    private fun getCurrentIsoTimestamp(): String {
        return java.time.Instant.now().toString()
    }
}
