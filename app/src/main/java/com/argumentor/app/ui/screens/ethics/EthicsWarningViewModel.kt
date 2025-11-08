package com.argumentor.app.ui.screens.ethics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argumentor.app.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EthicsWarningViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    fun markEthicsWarningAsShown() {
        viewModelScope.launch {
            settingsDataStore.setEthicsWarningShown(true)
        }
    }
}
