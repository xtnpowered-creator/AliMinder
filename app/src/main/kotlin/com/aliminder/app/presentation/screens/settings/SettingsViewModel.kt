package com.aliminder.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliminder.app.domain.model.UserSettings
import com.aliminder.app.domain.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = userSettingsRepository.getUserSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun updateDynamicTitleBarColor(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateDynamicTitleBarColor(enabled)
        }
    }

    fun updateUrgencyTimeThreshold(minutes: Int) {
        viewModelScope.launch {
            userSettingsRepository.updateUrgencyTimeThreshold(minutes)
        }
    }

    fun updateAutoHideOverdueMinutes(minutes: Int) {
        viewModelScope.launch {
            userSettingsRepository.updateAutoHideOverdueMinutes(minutes)
        }
    }

    fun updateHomeAddress(address: String) {
        viewModelScope.launch {
            userSettingsRepository.updateHomeAddress(address)
        }
    }

    fun updateWorkAddress(address: String) {
        viewModelScope.launch {
            userSettingsRepository.updateWorkAddress(address)
        }
    }
}
