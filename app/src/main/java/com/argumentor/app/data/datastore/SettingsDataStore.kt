package com.argumentor.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.argumentor.app.data.model.Topic
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * DataStore-based settings repository for application preferences.
 *
 * Provides type-safe access to user preferences using Jetpack DataStore.
 * All preferences are stored persistently and exposed as [Flow]s for reactive updates.
 *
 * Key features:
 * - Dark theme toggle
 * - Immersive mode (full-screen)
 * - Font size customization
 * - Ethics warning display control
 * - Default debate posture
 * - Onboarding and tutorial state
 *
 * Note: Some critical settings (onboarding, first launch) use dual-write strategy
 * with SharedPreferences for synchronous access during app initialization.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val SHOW_ETHICS_WARNING = booleanPreferencesKey("show_ethics_warning")
        val DEFAULT_POSTURE = stringPreferencesKey("default_posture")
        val ETHICS_WARNING_SHOWN = booleanPreferencesKey("ethics_warning_shown")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")
        val TUTORIAL_ENABLED = booleanPreferencesKey("tutorial_enabled")
        val DEMO_TOPIC_ID = stringPreferencesKey("demo_subject_id") // Key kept as "demo_subject_id" for backward compatibility
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_THEME] ?: false
    }

    val isImmersiveMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IMMERSIVE_MODE] ?: false
    }

    val fontSize: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SIZE] ?: "MEDIUM"
    }

    val showEthicsWarning: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_ETHICS_WARNING] ?: true
    }

    val defaultPosture: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEFAULT_POSTURE] ?: Topic.Posture.NEUTRAL_CRITICAL.toString()
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

    val demoTopicId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DEMO_TOPIC_ID]
    }

    /**
     * Updates the dark theme preference.
     * @param isDark true to enable dark theme, false for light theme
     */
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    /**
     * Updates the immersive mode preference (full-screen with hidden system bars).
     * @param enabled true to enable immersive mode, false to disable
     */
    suspend fun setImmersiveMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMMERSIVE_MODE] = enabled
        }
    }

    /**
     * Updates the font size preference.
     * @param size Font size identifier (e.g., "SMALL", "MEDIUM", "LARGE", "EXTRA_LARGE")
     */
    suspend fun setFontSize(size: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size
        }
    }

    /**
     * Updates whether the ethics warning should be shown in the app.
     * @param show true to show ethics warning, false to hide
     */
    suspend fun setShowEthicsWarning(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ETHICS_WARNING] = show
        }
    }

    /**
     * Updates the default debate posture for new topics.
     * @param posture Posture identifier (e.g., "neutral_critical", "skeptical", "academic_comparative")
     */
    suspend fun setDefaultPosture(posture: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_POSTURE] = posture
        }
    }

    /**
     * Records that the ethics warning has been shown to the user.
     * @param shown true if the warning has been shown
     */
    suspend fun setEthicsWarningShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ETHICS_WARNING_SHOWN] = shown
        }
    }

    /**
     * Updates the onboarding completion status.
     *
     * Uses dual-write strategy:
     * 1. Writes to SharedPreferences synchronously for fast access during app initialization
     * 2. Writes to DataStore as the source of truth
     * 3. Rolls back SharedPreferences if DataStore write fails
     *
     * @param completed true if onboarding has been completed
     * @throws Exception if DataStore write fails (after rollback)
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        try {
            // Write to SharedPreferences first (fast, synchronous)
            context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", completed)
                .commit()  // Use commit() for synchronous write

            // Then write to DataStore (source of truth)
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
            }
        } catch (e: Exception) {
            // Rollback SharedPreferences on DataStore failure
            context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", !completed)
                .commit()
            throw e
        }
    }

    /**
     * Updates the first launch completion status.
     *
     * Uses dual-write strategy:
     * 1. Writes to SharedPreferences synchronously for fast access during app initialization
     * 2. Writes to DataStore as the source of truth
     * 3. Rolls back SharedPreferences if DataStore write fails
     *
     * @param completed true if first launch has been completed
     * @throws Exception if DataStore write fails (after rollback)
     */
    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        try {
            // Write to SharedPreferences first (fast, synchronous)
            context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("first_launch_completed", completed)
                .commit()  // Use commit() for synchronous write

            // Then write to DataStore (source of truth)
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.FIRST_LAUNCH_COMPLETED] = completed
            }
        } catch (e: Exception) {
            // Rollback SharedPreferences on DataStore failure
            context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("first_launch_completed", !completed)
                .commit()
            throw e
        }
    }

    /**
     * Updates whether the tutorial is enabled.
     * When enabled, a demo topic is shown to help users understand the app.
     *
     * @param enabled true to enable tutorial, false to disable
     */
    suspend fun setTutorialEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TUTORIAL_ENABLED] = enabled
        }
    }

    /**
     * Updates the ID of the demo topic used for tutorial purposes.
     *
     * @param id The topic ID to use for demo, or null to remove
     */
    suspend fun setDemoTopicId(id: String?) {
        context.dataStore.edit { preferences ->
            if (id != null) {
                preferences[PreferencesKeys.DEMO_TOPIC_ID] = id
            } else {
                preferences.remove(PreferencesKeys.DEMO_TOPIC_ID)
            }
        }
    }
}
