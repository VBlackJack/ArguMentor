package com.argumentor.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper for managing application locale/language.
 *
 * IMPORTANT NOTES:
 * - This approach is considered legacy for modern Android apps
 * - For Android 13+ (API 33+), prefer using `AppCompatDelegate.setApplicationLocales()`
 * - For per-app language preferences, use the AndroidX AppCompat library
 *
 * THREAD SAFETY:
 * - Locale.setDefault() modifies JVM-wide state and is synchronized to prevent race conditions
 * - Multiple threads calling setLocale() concurrently will be serialized
 *
 * TODO: Migrate to modern per-app language preferences
 * See: https://developer.android.com/guide/topics/resources/app-languages
 */
object LocaleHelper {

    // Synchronization lock for thread-safe locale changes
    private val lock = Any()

    /**
     * Sets the application locale and returns a new Context with the updated configuration.
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

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                updateResources(context, locale)
            } else {
                updateResourcesLegacy(context, locale)
            }
        }
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
