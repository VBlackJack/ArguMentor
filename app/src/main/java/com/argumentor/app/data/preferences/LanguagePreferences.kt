package com.argumentor.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Available languages in the app
 */
enum class AppLanguage(val code: String, val displayName: String, val locale: Locale) {
    FRENCH("fr", "Français", Locale("fr", "FR")),
    ENGLISH("en", "English", Locale("en", "US"));

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code == code } ?: FRENCH
        }
    }
    
    /**
     * Get language code in format suitable for Android Speech Recognition
     */
    fun getSpeechLanguageCode(): String {
        return "${locale.language}-${locale.country}"
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
        private const val SHARED_PREFS_NAME = "app_language_prefs"
        private const val LANGUAGE_CODE_KEY = "language_code"
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
        // Save to DataStore
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.code
        }
        
        // Also save to SharedPreferences for quick access in attachBaseContext
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_CODE_KEY, language.code)
            .apply()
    }

    /**
     * Get current language synchronously (for use in non-suspending contexts)
     * Returns FRENCH as default if not set
     */
    suspend fun getCurrentLanguage(): AppLanguage {
        val preferences = context.dataStore.data.first()
        val code = preferences[LANGUAGE_KEY] ?: AppLanguage.FRENCH.code
        return AppLanguage.fromCode(code)
    }
}
