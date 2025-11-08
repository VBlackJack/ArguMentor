package com.argumentor.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _fontSize = MutableStateFlow(FontSize.MEDIUM)
    val fontSize: StateFlow<FontSize> = _fontSize.asStateFlow()

    private val _showEthicsWarning = MutableStateFlow(true)
    val showEthicsWarning: StateFlow<Boolean> = _showEthicsWarning.asStateFlow()

    private val _defaultPosture = MutableStateFlow("neutral_critique")
    val defaultPosture: StateFlow<String> = _defaultPosture.asStateFlow()

    init {
        // Load settings from DataStore (placeholder for now)
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // TODO: Load from DataStore
        }
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        saveSettings()
    }

    fun setFontSize(size: FontSize) {
        _fontSize.value = size
        saveSettings()
    }

    fun toggleEthicsWarning() {
        _showEthicsWarning.value = !_showEthicsWarning.value
        saveSettings()
    }

    fun setDefaultPosture(posture: String) {
        _defaultPosture.value = posture
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            // TODO: Save to DataStore
        }
    }

    enum class FontSize(val scale: Float) {
        SMALL(0.85f),
        MEDIUM(1.0f),
        LARGE(1.15f),
        EXTRA_LARGE(1.3f)
    }
}
