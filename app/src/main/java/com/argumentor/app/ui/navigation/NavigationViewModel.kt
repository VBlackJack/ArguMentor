package com.argumentor.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.argumentor.app.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val firstLaunchCompleted: Flow<Boolean> = settingsDataStore.firstLaunchCompleted
    val onboardingCompleted: Flow<Boolean> = settingsDataStore.onboardingCompleted
}
