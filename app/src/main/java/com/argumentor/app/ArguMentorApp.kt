package com.argumentor.app

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.argumentor.app.util.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Application class for ArguMentor.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 * Implements Configuration.Provider to provide custom WorkManager configuration.
 *
 * Security: Handles locale configuration and ensures proper initialization order.
 */
@HiltAndroidApp
class ArguMentorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Lazy configuration to avoid race condition with Hilt injection
    private val _workManagerConfiguration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    companion object {
        // Constants for SharedPreferences to avoid hardcoded strings
        private const val PREFS_NAME = "app_language_prefs"
        private const val PREF_LANGUAGE_CODE = "language_code"
        private const val DEFAULT_LANGUAGE = "fr"

        // Supported language codes
        private val SUPPORTED_LANGUAGES = setOf("fr", "en")
    }

    override fun attachBaseContext(base: Context) {
        try {
            // Read language from SharedPreferences with validation
            val prefs = base.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val languageCode = prefs.getString(PREF_LANGUAGE_CODE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE

            // Validate language code for security
            val validatedLanguageCode = if (languageCode in SUPPORTED_LANGUAGES) {
                languageCode
            } else {
                Timber.w("Invalid language code: $languageCode, falling back to $DEFAULT_LANGUAGE")
                DEFAULT_LANGUAGE
            }

            val locale = when (validatedLanguageCode) {
                "en" -> java.util.Locale("en", "US")
                "fr" -> java.util.Locale("fr", "FR")
                else -> java.util.Locale("fr", "FR") // Fallback
            }

            val context = LocaleHelper.setLocale(base, locale)
            super.attachBaseContext(context)
        } catch (e: Exception) {
            // Critical error handling - fallback to default context
            Timber.e(e, "Failed to set locale, using default context")
            super.attachBaseContext(base)
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging in both debug and release builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("ArguMentor Application initialized in DEBUG mode")
        } else {
            // In release, plant a production tree that logs only errors
            // TODO: Integrate crash reporting (Firebase Crashlytics, Sentry, etc.)
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority >= android.util.Log.ERROR) {
                        // Log only errors in production
                        // Future: Send to crash reporting service
                        android.util.Log.e(tag, message, t)
                    }
                }
            })
        }
    }

    /**
     * Provides WorkManager configuration with custom worker factory.
     * Uses lazy initialization to ensure workerFactory is injected by Hilt before access.
     */
    override val workManagerConfiguration: Configuration
        get() = _workManagerConfiguration
}
