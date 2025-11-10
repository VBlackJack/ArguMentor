package com.argumentor.app.data.util

import android.content.Context
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.preferences.AppLanguage
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.repository.TopicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TutorialManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val languagePreferences: LanguagePreferences,
    private val sampleDataGenerator: SampleDataGenerator,
    private val topicRepository: TopicRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_LANGUAGE = "last_language"
    }

    fun checkAndHandleLanguageChange() {
        scope.launch {
            val currentLanguage = languagePreferences.getCurrentLanguage()
            val lastLanguage = getLastLanguage()

            // If language has changed and tutorial is enabled, replace demo topic
            if (lastLanguage != null && lastLanguage != currentLanguage) {
                val tutorialEnabled = settingsDataStore.tutorialEnabled.first()
                if (tutorialEnabled) {
                    sampleDataGenerator.replaceDemoTopic()
                }
            }

            // Save current language for next comparison
            saveLastLanguage(currentLanguage)
        }
    }

    fun handleTutorialToggle(enabled: Boolean) {
        scope.launch {
            if (!enabled) {
                // Tutorial disabled - remove demo topic
                val demoTopicId = settingsDataStore.demoSubjectId.first()
                if (demoTopicId != null) {
                    topicRepository.deleteTopic(demoTopicId)
                    settingsDataStore.setDemoSubjectId(null)
                }
            } else {
                // Tutorial enabled - generate demo topic if it doesn't exist
                val demoTopicId = settingsDataStore.demoSubjectId.first()
                if (demoTopicId == null) {
                    sampleDataGenerator.generateSampleData()
                }
            }
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
