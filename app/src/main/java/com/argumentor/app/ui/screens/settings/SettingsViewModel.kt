package com.argumentor.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.datastore.SettingsDataStore
import com.argumentor.app.data.preferences.LanguagePreferences
import com.argumentor.app.data.preferences.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val languagePreferences: LanguagePreferences
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _fontSize = MutableStateFlow(FontSize.MEDIUM)
    val fontSize: StateFlow<FontSize> = _fontSize.asStateFlow()

    private val _showEthicsWarning = MutableStateFlow(true)
    val showEthicsWarning: StateFlow<Boolean> = _showEthicsWarning.asStateFlow()

    private val _defaultPosture = MutableStateFlow("neutral_critique")
    val defaultPosture: StateFlow<String> = _defaultPosture.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.FRENCH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()
    
    private val _pendingLanguage = MutableStateFlow<AppLanguage?>(null)
    val pendingLanguage: StateFlow<AppLanguage?> = _pendingLanguage.asStateFlow()
    
    private val _languageSaveCompleted = MutableStateFlow(false)
    val languageSaveCompleted: StateFlow<Boolean> = _languageSaveCompleted.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.isDarkTheme.collect { isDark ->
                _isDarkTheme.value = isDark
            }
        }
        viewModelScope.launch {
            settingsDataStore.fontSize.collect { size ->
                _fontSize.value = FontSize.valueOf(size)
            }
        }
        viewModelScope.launch {
            settingsDataStore.showEthicsWarning.collect { show ->
                _showEthicsWarning.value = show
            }
        }
        viewModelScope.launch {
            settingsDataStore.defaultPosture.collect { posture ->
                _defaultPosture.value = posture
            }
        }
        viewModelScope.launch {
            languagePreferences.languageFlow.collect { lang ->
                _language.value = lang
            }
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            val newValue = !_isDarkTheme.value
            _isDarkTheme.value = newValue
            settingsDataStore.setDarkTheme(newValue)
        }
    }

    fun setFontSize(size: FontSize) {
        viewModelScope.launch {
            _fontSize.value = size
            settingsDataStore.setFontSize(size.name)
        }
    }

    fun toggleEthicsWarning() {
        viewModelScope.launch {
            val newValue = !_showEthicsWarning.value
            _showEthicsWarning.value = newValue
            settingsDataStore.setShowEthicsWarning(newValue)
        }
    }

    fun setDefaultPosture(posture: String) {
        viewModelScope.launch {
            _defaultPosture.value = posture
            settingsDataStore.setDefaultPosture(posture)
        }
    }

    fun selectLanguage(language: AppLanguage) {
        if (language != _language.value) {
            _pendingLanguage.value = language
        } else {
            _pendingLanguage.value = null
        }
    }
    
    fun applyLanguageChange(onComplete: () -> Unit) {
        viewModelScope.launch {
            _pendingLanguage.value?.let { newLang ->
                languagePreferences.setLanguage(newLang)
                _language.value = newLang
                _pendingLanguage.value = null
                // Notify completion
                onComplete()
            }
        }
    }
    
    fun cancelLanguageChange() {
        _pendingLanguage.value = null
    }

    enum class FontSize(val scale: Float) {
        SMALL(0.85f),
        MEDIUM(1.0f),
        LARGE(1.15f),
        EXTRA_LARGE(1.3f)
    }
}
