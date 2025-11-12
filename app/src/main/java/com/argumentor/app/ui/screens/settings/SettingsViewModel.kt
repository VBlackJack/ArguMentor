package com.argumentor.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.preferences.AppLanguage
import com.argumentor.app.data.util.TutorialManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for app settings.
 *
 * MEMORY LEAK FIX: Uses stateIn() with WhileSubscribed to automatically stop
 * flow collection when there are no active observers, preventing memory leaks.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val languagePreferences: LanguagePreferences,
    private val tutorialManager: TutorialManager
) : ViewModel() {

    // Convert DataStore flows to StateFlows with automatic lifecycle management
    val isDarkTheme: StateFlow<Boolean> = settingsDataStore.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isImmersiveMode: StateFlow<Boolean> = settingsDataStore.isImmersiveMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val fontSize: StateFlow<FontSize> = settingsDataStore.fontSize
        .map { FontSize.valueOf(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FontSize.MEDIUM
        )

    val showEthicsWarning: StateFlow<Boolean> = settingsDataStore.showEthicsWarning
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val defaultPosture: StateFlow<String> = settingsDataStore.defaultPosture
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "neutral_critique"
        )

    val language: StateFlow<AppLanguage> = languagePreferences.languageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguage.FRENCH
        )

    val tutorialEnabled: StateFlow<Boolean> = settingsDataStore.tutorialEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val demoTopicId: StateFlow<String?> = settingsDataStore.demoTopicId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _pendingLanguage = MutableStateFlow<AppLanguage?>(null)
    val pendingLanguage: StateFlow<AppLanguage?> = _pendingLanguage.asStateFlow()

    private val _languageSaveCompleted = MutableStateFlow(false)
    val languageSaveCompleted: StateFlow<Boolean> = _languageSaveCompleted.asStateFlow()

    fun toggleDarkTheme() {
        viewModelScope.launch {
            val newValue = !isDarkTheme.value
            settingsDataStore.setDarkTheme(newValue)
        }
    }

    fun toggleImmersiveMode() {
        viewModelScope.launch {
            val newValue = !isImmersiveMode.value
            settingsDataStore.setImmersiveMode(newValue)
        }
    }

    fun setFontSize(size: FontSize) {
        viewModelScope.launch {
            settingsDataStore.setFontSize(size.name)
        }
    }

    fun toggleEthicsWarning() {
        viewModelScope.launch {
            val newValue = !showEthicsWarning.value
            settingsDataStore.setShowEthicsWarning(newValue)
        }
    }

    fun setDefaultPosture(posture: String) {
        viewModelScope.launch {
            settingsDataStore.setDefaultPosture(posture)
        }
    }

    fun selectLanguage(newLanguage: AppLanguage) {
        if (newLanguage != language.value) {
            _pendingLanguage.value = newLanguage
        } else {
            _pendingLanguage.value = null
        }
    }

    fun applyLanguageChange(onComplete: () -> Unit) {
        viewModelScope.launch {
            _pendingLanguage.value?.let { newLang ->
                languagePreferences.setLanguage(newLang)
                _pendingLanguage.value = null
                // Notify completion
                onComplete()
            }
        }
    }

    fun cancelLanguageChange() {
        _pendingLanguage.value = null
    }

    fun toggleTutorialEnabled() {
        viewModelScope.launch {
            val newValue = !tutorialEnabled.value
            settingsDataStore.setTutorialEnabled(newValue)
            tutorialManager.handleTutorialToggle(newValue)
        }
    }

    enum class FontSize(val scale: Float) {
        SMALL(0.85f),
        MEDIUM(1.0f),
        LARGE(1.15f),
        EXTRA_LARGE(1.3f)
    }
}
