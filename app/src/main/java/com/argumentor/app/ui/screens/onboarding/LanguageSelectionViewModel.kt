package com.argumentor.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.preferences.AppLanguage
import com.argumentor.app.data.preferences.LanguagePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val languagePreferences: LanguagePreferences,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    fun selectLanguage(language: AppLanguage, onComplete: () -> Unit) {
        viewModelScope.launch {
            // Set the selected language
            languagePreferences.setLanguage(language)

            // Mark first launch as completed
            settingsDataStore.setFirstLaunchCompleted(true)

            // Trigger navigation callback
            onComplete()
        }
    }
}
