package com.argumentor.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.util.SampleDataGenerator
import com.argumentor.app.data.util.TutorialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val sampleDataGenerator: SampleDataGenerator,
    private val tutorialManager: TutorialManager
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    /**
     * MEMORY LEAK FIX: Use stateIn with WhileSubscribed to avoid infinite collection.
     * Flow only collects while there are active subscribers (UI visible).
     */
    val tutorialEnabled: StateFlow<Boolean> = settingsDataStore.tutorialEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = true
        )

    val totalPages = 4

    fun toggleTutorial() {
        viewModelScope.launch {
            val newValue = !tutorialEnabled.value
            settingsDataStore.setTutorialEnabled(newValue)
            tutorialManager.handleTutorialToggle(newValue)
        }
    }

    fun nextPage() {
        if (_currentPage.value < totalPages - 1) {
            _currentPage.value++
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
        }
    }

    fun skipOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            // Generate sample data when skipping to help users understand the app
            sampleDataGenerator.generateSampleData()
            onComplete()
        }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            // Generate sample data to help users get started
            // Must complete BEFORE disabling tutorial
            sampleDataGenerator.generateSampleData()
            // Now it's safe to disable tutorial
            // User can re-enable it in Settings to see it again
            settingsDataStore.setTutorialEnabled(false)
            onComplete()
        }
    }
}
