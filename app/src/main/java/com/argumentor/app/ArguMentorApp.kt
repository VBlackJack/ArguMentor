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
 */
@HiltAndroidApp
class ArguMentorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun attachBaseContext(base: Context) {
        // Read language from SharedPreferences
        val prefs = base.getSharedPreferences("app_language_prefs", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language_code", "fr") ?: "fr"

        val locale = when (languageCode) {
            "en" -> java.util.Locale("en", "US")
            "fr" -> java.util.Locale("fr", "FR")
            else -> java.util.Locale("fr", "FR")
        }

        val context = LocaleHelper.setLocale(base, locale)
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("ArguMentor Application initialized in DEBUG mode")
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
