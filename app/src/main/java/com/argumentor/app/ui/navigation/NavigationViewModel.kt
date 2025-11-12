package com.argumentor.app.ui.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // Load initial values synchronously from SharedPreferences to avoid flash
    private val prefs = context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)

    /**
     * MEMORY LEAK FIX: Use stateIn with Eagerly for navigation state that must always be available.
     * Initial value loaded from SharedPreferences cache to avoid flash.
     * Note: Using Eagerly instead of WhileSubscribed because navigation state is critical
     * and must be immediately available for app navigation decisions.
     */
    val firstLaunchCompleted: StateFlow<Boolean> = settingsDataStore.firstLaunchCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = prefs.getBoolean("first_launch_completed", false)
        )

    val ethicsWarningShown: StateFlow<Boolean> = settingsDataStore.ethicsWarningShown
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = prefs.getBoolean("ethics_warning_shown", false)
        )

    val onboardingCompleted: StateFlow<Boolean> = settingsDataStore.onboardingCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = prefs.getBoolean("onboarding_completed", false)
        )
}
