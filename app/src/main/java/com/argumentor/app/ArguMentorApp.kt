package com.argumentor.app

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.argumentor.app.data.repository.FallacyRepository
import com.argumentor.app.util.AppConstants
import com.argumentor.app.util.LocaleHelper
import com.argumentor.app.util.ProductionTree
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var fallacyRepository: FallacyRepository

    // Application-scoped coroutine scope for background initialization tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Lazy configuration to avoid race condition with Hilt injection
    private val _workManagerConfiguration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun attachBaseContext(base: Context) {
        try {
            // ISSUE-005 FIX: Now uses AppConstants for consistent configuration
            val prefs = base.getSharedPreferences(
                AppConstants.Language.PREFS_NAME,
                Context.MODE_PRIVATE
            )
            val languageCode = prefs.getString(
                AppConstants.Language.PREF_KEY_LANGUAGE_CODE,
                AppConstants.Language.DEFAULT_LANGUAGE
            )

            // Validate language code for security using centralized validation
            val validatedLanguageCode = AppConstants.Language.getValidatedLanguageCode(languageCode)

            // Log warning if invalid language code was detected
            if (languageCode != null && languageCode != validatedLanguageCode) {
                Timber.w("Invalid language code: $languageCode, falling back to $validatedLanguageCode")
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
            // In release, plant a production tree with crash reporting capabilities
            Timber.plant(ProductionTree())
        }

        // BUG FIX: Initialize default fallacies on first launch
        // The migration MIGRATION_9_10 only runs when upgrading from version 9 to 10.
        // Users who install the app for the first time after version 10 would have
        // an empty fallacies table. This ensures the 30 default fallacies are always
        // available, even on fresh installations.
        applicationScope.launch {
            fallacyRepository.ensureDefaultFallaciesExist()
        }
    }

    /**
     * Provides WorkManager configuration with custom worker factory.
     * Uses lazy initialization to ensure workerFactory is injected by Hilt before access.
     */
    override val workManagerConfiguration: Configuration
        get() = _workManagerConfiguration
}
