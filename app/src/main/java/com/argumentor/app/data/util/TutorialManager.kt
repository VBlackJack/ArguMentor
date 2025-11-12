package com.argumentor.app.data.util

import android.content.Context
import androidx.room.withTransaction
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.local.ArguMentorDatabase
import com.argumentor.app.data.preferences.AppLanguage
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorialManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: ArguMentorDatabase,
    private val settingsDataStore: SettingsDataStore,
    private val languagePreferences: LanguagePreferences,
    private val sampleDataGenerator: SampleDataGenerator,
    private val topicRepository: TopicRepository,
    private val claimRepository: ClaimRepository,
    private val questionRepository: QuestionRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_LANGUAGE = "last_language"
        private const val KEY_DEMO_TOPIC_REPLACED = "demo_topic_replaced"
    }

    fun checkAndHandleLanguageChange() {
        scope.launch {
            val currentLanguage = languagePreferences.getCurrentLanguage()
            val lastLanguage = getLastLanguage()
            val tutorialEnabled = settingsDataStore.tutorialEnabled.first()
            val demoTopicId = settingsDataStore.demoTopicId.first()

            // Regenerate demo topic if:
            // 1. Language has changed (including first launch where lastLanguage is null)
            // 2. AND either tutorial is enabled OR a demo topic already exists
            // This ensures demo topic is translated even if user disabled tutorial after creation
            if (lastLanguage != currentLanguage) {
                if (tutorialEnabled || demoTopicId != null) {
                    sampleDataGenerator.replaceDemoTopic()
                    // Store flag to show notification
                    setDemoTopicReplaced(true)
                } else {
                    setDemoTopicReplaced(false)
                }
            } else {
                setDemoTopicReplaced(false)
            }

            // Save current language for next comparison
            saveLastLanguage(currentLanguage)
        }
    }

    fun getDemoTopicReplaced(): Boolean {
        return prefs.getBoolean(KEY_DEMO_TOPIC_REPLACED, false)
    }

    fun clearDemoTopicReplacedFlag() {
        prefs.edit().putBoolean(KEY_DEMO_TOPIC_REPLACED, false).apply()
    }

    private fun setDemoTopicReplaced(replaced: Boolean) {
        prefs.edit().putBoolean(KEY_DEMO_TOPIC_REPLACED, replaced).apply()
    }

    fun handleTutorialToggle(enabled: Boolean) {
        scope.launch {
            if (!enabled) {
                // Tutorial disabled - remove demo topic and all related data
                val demoTopicId = settingsDataStore.demoTopicId.first()
                if (demoTopicId != null) {
                    deleteDemoTopicCompletely(demoTopicId)
                    settingsDataStore.setDemoTopicId(null)
                }
            } else {
                // Tutorial enabled - always regenerate demo topic
                // First, delete existing demo topic if any
                val existingDemoTopicId = settingsDataStore.demoTopicId.first()
                if (existingDemoTopicId != null) {
                    deleteDemoTopicCompletely(existingDemoTopicId)
                    settingsDataStore.setDemoTopicId(null)
                }

                // Then generate new demo topic
                sampleDataGenerator.generateSampleData()
            }
        }
    }

    /**
     * Delete demo topic and all its related entities atomically.
     * Uses a database transaction to ensure data consistency.
     *
     * BUGFIX: Removed delay() from inside transaction to prevent potential deadlocks.
     * Room's foreign key cascades and the transaction mechanism handle deletions atomically,
     * so no delay is needed. The database transaction ensures all operations complete
     * successfully or are rolled back together.
     */
    private suspend fun deleteDemoTopicCompletely(topicId: String) {
        database.withTransaction {
            // Get all claims for this topic
            val claims = claimRepository.getClaimsForTopic(topicId)

            // Delete each claim (this will cascade delete rebuttals and evidence)
            // Or if claim belongs to multiple topics, just remove this topicId
            claims.forEach { claim ->
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

            // Finally, delete the topic itself
            // The transaction ensures all previous operations complete successfully
            // before this executes, or everything is rolled back on failure
            topicRepository.deleteTopicById(topicId)
        }
    }

    private fun getLastLanguage(): AppLanguage? {
        val languageCode = prefs.getString(KEY_LAST_LANGUAGE, null) ?: return null
        return AppLanguage.values().find { it.code == languageCode }
    }

    private fun saveLastLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LAST_LANGUAGE, language.code).apply()
    }
}
