package com.argumentor.app.ui.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import com.argumentor.app.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // Load initial values synchronously from SharedPreferences to avoid flash
    private val prefs = context.getSharedPreferences("settings_cache", Context.MODE_PRIVATE)

    private val _firstLaunchCompleted = MutableStateFlow(
        prefs.getBoolean("first_launch_completed", false)
    )
    val firstLaunchCompleted: StateFlow<Boolean> = _firstLaunchCompleted.asStateFlow()

    private val _ethicsWarningShown = MutableStateFlow(
        prefs.getBoolean("ethics_warning_shown", false)
    )
    val ethicsWarningShown: StateFlow<Boolean> = _ethicsWarningShown.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_completed", false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()
}
