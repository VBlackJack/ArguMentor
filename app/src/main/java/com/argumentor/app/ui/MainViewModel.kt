package com.argumentor.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsDataStore: SettingsDataStore
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = settingsDataStore.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val ethicsWarningShown: StateFlow<Boolean> = settingsDataStore.ethicsWarningShown
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}
