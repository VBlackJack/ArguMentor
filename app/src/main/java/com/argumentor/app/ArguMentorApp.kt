package com.argumentor.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for ArguMentor.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class ArguMentorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize components here if needed
    }
}
