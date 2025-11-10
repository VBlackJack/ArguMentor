package com.argumentor.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.argumentor.app.data.preferences.LanguagePreferences
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel to provide the current locale to composables
 */
@HiltViewModel
class LocaleViewModel @Inject constructor(
    private val languagePreferences: LanguagePreferences
) : ViewModel() {
    val locale: StateFlow<Locale> = languagePreferences.languageFlow
        .map { appLanguage -> appLanguage.locale }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Locale.FRENCH
        )
}

/**
 * Composable that provides the current locale based on user preferences
 */
@Composable
fun rememberCurrentLocale(
    viewModel: LocaleViewModel = hiltViewModel()
): Locale {
    val locale by viewModel.locale.collectAsState()
    return locale
}
