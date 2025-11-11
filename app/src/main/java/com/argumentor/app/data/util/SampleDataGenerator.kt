package com.argumentor.app.data.util

import android.content.Context
import com.argumentor.app.R
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.model.*
import com.argumentor.app.data.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val rebuttalRepository: RebuttalRepository,
    private val evidenceRepository: EvidenceRepository,
    private val questionRepository: QuestionRepository,
    private val sourceRepository: SourceRepository,
    private val settingsDataStore: SettingsDataStore
) {

    suspend fun generateSampleData() {
        // Check if tutorial is enabled
        val tutorialEnabled = settingsDataStore.tutorialEnabled.first()
        if (!tutorialEnabled) {
            return
        }

        // Check if demo topic already exists
        val demoTopicId = settingsDataStore.demoSubjectId.first()
        if (demoTopicId != null) {
            // Demo topic already exists, don't duplicate
            return
        }

        // Generate the demo topic
        createDemoTopic()
    }

    suspend fun replaceDemoTopic() {
        // Check if tutorial is enabled
        val tutorialEnabled = settingsDataStore.tutorialEnabled.first()
        if (!tutorialEnabled) {
            return
        }

        // Delete existing demo topic if it exists
        val existingDemoTopicId = settingsDataStore.demoSubjectId.first()
        if (existingDemoTopicId != null) {
            deleteDemoTopicCompletely(existingDemoTopicId)
            settingsDataStore.setDemoSubjectId(null)
        }

        // Create new demo topic in current language
        createDemoTopic()
    }

    private suspend fun createDemoTopic() {
        // Get localized strings
        val tags = context.resources.getStringArray(R.array.demo_topic_tags).toList()

        // Create topic
        val topic = Topic(
            title = context.getString(R.string.demo_topic_title),
            summary = context.getString(R.string.demo_topic_summary),
            posture = Topic.Posture.NEUTRAL_CRITICAL,
            tags = tags
        )
        topicRepository.insertTopic(topic)

        // Store the demo topic ID
        settingsDataStore.setDemoSubjectId(topic.id)

        // Create PRO claims
        val claim1 = Claim(
            topics = listOf(topic.id),
            text = context.getString(R.string.demo_claim_1),
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.HIGH
        )
        claimRepository.insertClaim(claim1)

        val claim2 = Claim(
            topics = listOf(topic.id),
            text = context.getString(R.string.demo_claim_2),
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.MEDIUM
        )
        claimRepository.insertClaim(claim2)

        // Create CON claims
        val claim3 = Claim(
            topics = listOf(topic.id),
            text = context.getString(R.string.demo_claim_3),
            stance = Claim.Stance.CON,
            strength = Claim.Strength.MEDIUM
        )
        claimRepository.insertClaim(claim3)

        val claim4 = Claim(
            topics = listOf(topic.id),
            text = context.getString(R.string.demo_claim_4),
            stance = Claim.Stance.CON,
            strength = Claim.Strength.LOW
        )
        claimRepository.insertClaim(claim4)

        // Add rebuttals (counter-arguments)
        val rebuttal1 = Rebuttal(
            claimId = claim3.id,
            text = context.getString(R.string.demo_rebuttal_1),
            fallacyIds = emptyList()
        )
        rebuttalRepository.insertRebuttal(rebuttal1)

        val rebuttal2 = Rebuttal(
            claimId = claim4.id,
            text = context.getString(R.string.demo_rebuttal_2),
            fallacyIds = emptyList()
        )
        rebuttalRepository.insertRebuttal(rebuttal2)

        // Add evidence
        val source1 = Source(
            title = context.getString(R.string.demo_source_title),
            citation = context.getString(R.string.demo_source_citation),
            url = context.getString(R.string.demo_source_url)
        )
        sourceRepository.insertSource(source1)

        val evidence1 = Evidence(
            claimId = claim1.id,
            content = context.getString(R.string.demo_evidence_1),
            type = Evidence.EvidenceType.STUDY,
            sourceId = source1.id
        )
        evidenceRepository.insertEvidence(evidence1)

        val evidence2 = Evidence(
            claimId = claim2.id,
            content = context.getString(R.string.demo_evidence_2),
            type = Evidence.EvidenceType.STAT,
            sourceId = null
        )
        evidenceRepository.insertEvidence(evidence2)

        // Add questions
        val question1 = Question(
            targetId = topic.id,
            text = context.getString(R.string.demo_question_1),
            kind = Question.QuestionKind.CLARIFYING
        )
        questionRepository.insertQuestion(question1)

        val question2 = Question(
            targetId = claim1.id,
            text = context.getString(R.string.demo_question_2),
            kind = Question.QuestionKind.SOCRATIC
        )
        questionRepository.insertQuestion(question2)

        val question3 = Question(
            targetId = claim2.id,
            text = context.getString(R.string.demo_question_3),
            kind = Question.QuestionKind.CHALLENGE
        )
        questionRepository.insertQuestion(question3)
    }

    /**
     * Delete demo topic and all its related entities
     */
    private suspend fun deleteDemoTopicCompletely(topicId: String) {
        // Get all claims for this topic
        val claims = claimRepository.getClaimsForTopic(topicId)

        // Collect evidence IDs to find associated sources later
        val evidenceIds = mutableSetOf<String>()

        // Delete each claim (this will cascade delete rebuttals and evidence)
        // Or if claim belongs to multiple topics, just remove this topicId
        claims.forEach { claim ->
            // Note: Evidence deletion is handled by cascade in Room database

            if (claim.topics.size == 1 && claim.topics.contains(topicId)) {
                // Claim only belongs to this topic, delete it completely
                claimRepository.deleteClaim(claim)
            } else if (claim.topics.contains(topicId)) {
                // Claim belongs to multiple topics, just remove this topicId
                val updatedClaim = claim.copy(topics = claim.topics - topicId)
                claimRepository.updateClaim(updatedClaim)
            }
        }

        // Delete all questions for this topic
        val questions = questionRepository.getQuestionsForTopic(topicId)
        questions.forEach { question ->
            questionRepository.deleteQuestion(question)
        }

        // Delete associated sources (demo sources only)
        // Get all sources and delete those created for demo
        val allSources = sourceRepository.getAllSources().first()
        allSources.forEach { source ->
            // Delete sources that match demo patterns (simplified check)
            if (source.title.contains("Démo", ignoreCase = true) ||
                source.title.contains("Demo", ignoreCase = true) ||
                source.title.contains("Tutorial", ignoreCase = true) ||
                source.title.contains("Tutoriel", ignoreCase = true)) {
                sourceRepository.deleteSource(source)
            }
        }

        // Finally, delete the topic itself
        topicRepository.deleteTopicById(topicId)
    }
}
