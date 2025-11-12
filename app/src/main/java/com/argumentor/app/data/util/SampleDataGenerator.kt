package com.argumentor.app.data.util

import android.content.Context
import android.content.res.Configuration
import com.argumentor.app.R
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.model.*
import com.argumentor.app.data.preferences.AppLanguage
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates sample/demo data for tutorial purposes.
 *
 * RACE CONDITION FIX:
 * - Uses Mutex to prevent concurrent calls from creating duplicate demo topics
 * - Ensures atomic check-and-create operations
 */
@Singleton
class SampleDataGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val rebuttalRepository: RebuttalRepository,
    private val evidenceRepository: EvidenceRepository,
    private val questionRepository: QuestionRepository,
    private val sourceRepository: SourceRepository,
    private val settingsDataStore: SettingsDataStore,
    private val languagePreferences: LanguagePreferences
) {
    // Mutex to prevent race conditions when creating demo topics
    private val demoTopicMutex = Mutex()

    suspend fun generateSampleData() = demoTopicMutex.withLock {
        // Check if tutorial is enabled
        val tutorialEnabled = settingsDataStore.tutorialEnabled.first()
        if (!tutorialEnabled) {
            return
        }

        // Check if demo topic already exists (atomic with creation)
        val demoTopicId = settingsDataStore.demoTopicId.first()
        if (demoTopicId != null) {
            // Demo topic already exists, don't duplicate
            return
        }

        // Get current language from preferences
        val currentLanguage = languagePreferences.getCurrentLanguage()

        // Generate the demo topic in the current language (still under mutex lock)
        createDemoTopic(currentLanguage)
    }

    suspend fun replaceDemoTopic(targetLanguage: AppLanguage) = demoTopicMutex.withLock {
        // Allow regeneration even if tutorial is disabled, as long as a demo topic exists
        // This ensures the demo topic is translated when language changes
        val tutorialEnabled = settingsDataStore.tutorialEnabled.first()
        val existingDemoTopicId = settingsDataStore.demoTopicId.first()

        if (!tutorialEnabled && existingDemoTopicId == null) {
            // Tutorial disabled and no existing demo topic, don't create one
            return@withLock
        }

        // Delete existing demo topic if it exists (atomic with creation)
        if (existingDemoTopicId != null) {
            deleteDemoTopicCompletely(existingDemoTopicId)
            settingsDataStore.setDemoTopicId(null)
            // HIGH-003 FIX: Also clear demo source ID when replacing demo topic
            // (it's already deleted in deleteDemoTopicCompletely, this is redundant but safe)
            settingsDataStore.setDemoSourceId(null)
        }

        // Create new demo topic in TARGET language (still under mutex lock)
        // This ensures the demo topic is created in the correct language, not the previous one
        createDemoTopic(targetLanguage)
    }

    private suspend fun createDemoTopic(targetLanguage: AppLanguage) {
        // BUGFIX: Create a context with the target locale to get correctly localized strings
        // This ensures the demo topic is created in the NEW language, not the old one
        val locale = Locale(targetLanguage.code)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        val localizedContext = context.createConfigurationContext(configuration)

        // Get localized strings using the localized context
        val tags = localizedContext.resources.getStringArray(R.array.demo_topic_tags).toList()

        // Create topic with strings in the target language
        val topic = Topic(
            title = localizedContext.getString(R.string.demo_topic_title),
            summary = localizedContext.getString(R.string.demo_topic_summary),
            posture = Topic.Posture.NEUTRAL_CRITICAL,
            tags = tags
        )
        topicRepository.insertTopic(topic)

        // Store the demo topic ID
        settingsDataStore.setDemoTopicId(topic.id)

        // Create PRO claims with localized strings
        val claim1 = Claim(
            topics = listOf(topic.id),
            text = localizedContext.getString(R.string.demo_claim_1),
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.HIGH
        )
        claimRepository.insertClaim(claim1)

        val claim2 = Claim(
            topics = listOf(topic.id),
            text = localizedContext.getString(R.string.demo_claim_2),
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.MEDIUM
        )
        claimRepository.insertClaim(claim2)

        // Create CON claims with localized strings
        val claim3 = Claim(
            topics = listOf(topic.id),
            text = localizedContext.getString(R.string.demo_claim_3),
            stance = Claim.Stance.CON,
            strength = Claim.Strength.MEDIUM
        )
        claimRepository.insertClaim(claim3)

        val claim4 = Claim(
            topics = listOf(topic.id),
            text = localizedContext.getString(R.string.demo_claim_4),
            stance = Claim.Stance.CON,
            strength = Claim.Strength.LOW
        )
        claimRepository.insertClaim(claim4)

        // Add rebuttals (counter-arguments) with localized strings
        val rebuttal1 = Rebuttal(
            claimId = claim3.id,
            text = localizedContext.getString(R.string.demo_rebuttal_1),
            fallacyIds = emptyList()
        )
        rebuttalRepository.insertRebuttal(rebuttal1)

        val rebuttal2 = Rebuttal(
            claimId = claim4.id,
            text = localizedContext.getString(R.string.demo_rebuttal_2),
            fallacyIds = emptyList()
        )
        rebuttalRepository.insertRebuttal(rebuttal2)

        // Add evidence with localized strings
        val source1 = Source(
            title = localizedContext.getString(R.string.demo_source_title),
            citation = localizedContext.getString(R.string.demo_source_citation),
            url = localizedContext.getString(R.string.demo_source_url)
        )
        sourceRepository.insertSource(source1)

        // HIGH-003 FIX: Store demo source ID for safe deletion
        settingsDataStore.setDemoSourceId(source1.id)

        val evidence1 = Evidence(
            claimId = claim1.id,
            content = localizedContext.getString(R.string.demo_evidence_1),
            type = Evidence.EvidenceType.STUDY,
            sourceId = source1.id
        )
        evidenceRepository.insertEvidence(evidence1)

        val evidence2 = Evidence(
            claimId = claim2.id,
            content = localizedContext.getString(R.string.demo_evidence_2),
            type = Evidence.EvidenceType.STAT,
            sourceId = null
        )
        evidenceRepository.insertEvidence(evidence2)

        // Add questions with localized strings
        val question1 = Question(
            targetId = topic.id,
            text = localizedContext.getString(R.string.demo_question_1),
            kind = Question.QuestionKind.CLARIFYING
        )
        questionRepository.insertQuestion(question1)

        val question2 = Question(
            targetId = claim1.id,
            text = localizedContext.getString(R.string.demo_question_2),
            kind = Question.QuestionKind.SOCRATIC
        )
        questionRepository.insertQuestion(question2)

        val question3 = Question(
            targetId = claim2.id,
            text = localizedContext.getString(R.string.demo_question_3),
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

        // HIGH-003 FIX: Delete demo source using stored ID instead of fragile pattern matching
        // This prevents accidental deletion of user-created sources with "Demo" in titles
        val demoSourceId = settingsDataStore.demoSourceId.first()
        if (demoSourceId != null) {
            try {
                sourceRepository.deleteSourceById(demoSourceId)
                settingsDataStore.setDemoSourceId(null)
            } catch (e: Exception) {
                // Source may already be deleted or not exist, continue
                Timber.w(e, "Failed to delete demo source: $demoSourceId")
            }
        }

        // Finally, delete the topic itself
        topicRepository.deleteTopicById(topicId)
    }
}
