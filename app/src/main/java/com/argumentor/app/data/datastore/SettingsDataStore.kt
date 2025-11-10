package com.argumentor.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val SHOW_ETHICS_WARNING = booleanPreferencesKey("show_ethics_warning")
        val DEFAULT_POSTURE = stringPreferencesKey("default_posture")
        val ETHICS_WARNING_SHOWN = booleanPreferencesKey("ethics_warning_shown")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")
        val TUTORIAL_ENABLED = booleanPreferencesKey("tutorial_enabled")
        val DEMO_SUBJECT_ID = stringPreferencesKey("demo_subject_id")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_THEME] ?: false
    }

    val fontSize: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SIZE] ?: "MEDIUM"
    }

    val showEthicsWarning: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_ETHICS_WARNING] ?: true
    }

    val defaultPosture: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_POSTURE] ?: "NEUTRAL_CRITIQUE"
    }

    val ethicsWarningShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ETHICS_WARNING_SHOWN] ?: false
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val firstLaunchCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_LAUNCH_COMPLETED] ?: false
    }

    val tutorialEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TUTORIAL_ENABLED] ?: true
    }

    val demoSubjectId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEMO_SUBJECT_ID]
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun setFontSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size
        }
    }

    suspend fun setShowEthicsWarning(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ETHICS_WARNING] = show
        }
    }

    suspend fun setDefaultPosture(posture: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_POSTURE] = posture
        }
    }

    suspend fun setEthicsWarningShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ETHICS_WARNING_SHOWN] = shown
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
        // Also save to SharedPreferences cache for fast startup
        context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", completed)
            .apply()
    }

    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH_COMPLETED] = completed
        }
        // Also save to SharedPreferences cache for fast startup
        context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("first_launch_completed", completed)
            .apply()
    }

    suspend fun setTutorialEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TUTORIAL_ENABLED] = enabled
        }
    }

    suspend fun setDemoSubjectId(id: String?) {
        context.dataStore.edit { preferences ->
            if (id != null) {
                preferences[PreferencesKeys.DEMO_SUBJECT_ID] = id
            } else {
                preferences.remove(PreferencesKeys.DEMO_SUBJECT_ID)
            }
        }
    }
}
