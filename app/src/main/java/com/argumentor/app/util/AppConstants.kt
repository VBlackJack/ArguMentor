package com.argumentor.app.util

/**
 * Application-wide constants for shared configuration.
 *
 * ISSUE-005 FIX: Centralized constants to eliminate duplication between
 * ArguMentorApp and MainActivity, following DRY principle.
 */
object AppConstants {

    /**
     * Language preferences configuration.
     * Used by both ArguMentorApp and MainActivity for consistent locale handling.
     */
    object Language {
        /**
         * SharedPreferences file name for language settings.
         */
        const val PREFS_NAME = "app_language_prefs"

        /**
         * Preference key for storing the selected language code.
         */
        const val PREF_KEY_LANGUAGE_CODE = "language_code"

        /**
         * Default language code when no preference is set.
         * French ("fr") is the default language for ArguMentor.
         */
        const val DEFAULT_LANGUAGE = "fr"

        /**
         * Set of supported language codes.
         * Only these languages are allowed to prevent security issues
         * and ensure proper localization support.
         *
         * Supported languages:
         * - "fr": French (France)
         * - "en": English (United States)
         */
        val SUPPORTED_LANGUAGES = setOf("fr", "en")

        /**
         * Validates a language code against the supported languages list.
         *
         * @param languageCode The language code to validate
         * @return true if the language code is supported, false otherwise
         */
        fun isSupported(languageCode: String): Boolean {
            return languageCode in SUPPORTED_LANGUAGES
        }

        /**
         * Gets a validated language code, falling back to default if invalid.
         *
         * @param languageCode The language code to validate
         * @return The validated language code, or DEFAULT_LANGUAGE if invalid
         */
        fun getValidatedLanguageCode(languageCode: String?): String {
            return if (languageCode != null && isSupported(languageCode)) {
                languageCode
            } else {
                DEFAULT_LANGUAGE
            }
        }
    }
}
