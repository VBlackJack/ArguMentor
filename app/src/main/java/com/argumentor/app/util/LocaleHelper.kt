package com.argumentor.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Helper for managing application locale/language.
 *
 * MODERNIZATION UPDATE:
 * - Now supports Android 13+ (API 33+) per-app language preferences via AppCompatDelegate
 * - Automatically uses the modern API when available
 * - Falls back to legacy implementation for older Android versions
 * - Integrates with system-wide per-app language settings on Android 13+
 *
 * THREAD SAFETY:
 * - Locale.setDefault() modifies JVM-wide state and is synchronized to prevent race conditions
 * - Multiple threads calling setLocale() concurrently will be serialized
 *
 * See: https://developer.android.com/guide/topics/resources/app-languages
 */
object LocaleHelper {

    // Synchronization lock for thread-safe locale changes
    private val lock = Any()

    /**
     * Sets the application locale using the best available API for the current Android version.
     *
     * Android 13+ (API 33+): Uses AppCompatDelegate.setApplicationLocales() which integrates
     * with system per-app language settings.
     *
     * Android 7-12 (API 24-32): Uses createConfigurationContext() for locale override.
     *
     * Android 6 and below (API < 24): Uses deprecated updateConfiguration() for compatibility.
     *
     * @param context The base context to apply locale to
     * @param locale The desired locale
     * @return A new Context with the applied locale configuration
     *
     * THREAD SAFETY: This method is synchronized to prevent concurrent modifications
     * to the global JVM default locale.
     */
    fun setLocale(context: Context, locale: Locale): Context {
        // Synchronize access to Locale.setDefault() which modifies JVM-wide state
        synchronized(lock) {
            Locale.setDefault(locale)

            // Use modern API on Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return setLocaleModern(context, locale)
            }

            // Use legacy context creation for Android 7-12
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                updateResources(context, locale)
            } else {
                updateResourcesLegacy(context, locale)
            }
        }
    }

    /**
     * Sets locale using modern AppCompatDelegate API (Android 13+ / API 33+).
     * This integrates with the system's per-app language settings.
     *
     * ADVANTAGE: User's language choice persists across app restarts and appears
     * in Android system settings under "App languages".
     */
    private fun setLocaleModern(context: Context, locale: Locale): Context {
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)

        // Still update context configuration for immediate effect
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * Updates resources for Android N (API 24) and above.
     * Uses createConfigurationContext() which is the recommended approach.
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * Updates resources for pre-Android N devices (API < 24).
     * Uses deprecated updateConfiguration() for backward compatibility.
     */
    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}
