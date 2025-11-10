package com.argumentor.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Available languages in the app
 */
enum class AppLanguage(val code: String, val displayName: String, val locale: Locale) {
    FRENCH("fr", "Français", Locale.FRENCH),
    ENGLISH("en", "English", Locale.ENGLISH);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code == code } ?: FRENCH
        }
    }
}

/**
 * Manager for language preferences.
 * Handles both UI language and voice input language.
 */
@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    /**
     * Flow of the current app language
     */
    val languageFlow: Flow<AppLanguage> = context.dataStore.data
        .map { preferences ->
            val code = preferences[LANGUAGE_KEY] ?: AppLanguage.FRENCH.code
            AppLanguage.fromCode(code)
        }

    /**
     * Set the app language
     */
    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
    }

    /**
     * Get current language synchronously (for use in non-suspending contexts)
     * Returns FRENCH as default if not set
     */
    suspend fun getCurrentLanguage(): AppLanguage {
        var result = AppLanguage.FRENCH
        context.dataStore.data.collect { preferences ->
            val code = preferences[LANGUAGE_KEY] ?: AppLanguage.FRENCH.code
            result = AppLanguage.fromCode(code)
        }
        return result
    }
}
