package com.aliminder.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliminder.app.domain.model.UserSettings
import com.aliminder.app.domain.repository.UserSettingsRepository
import com.aliminder.app.data.mapper.toDomainDuty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val dutyRepository: com.aliminder.app.domain.repository.DutyRepository,
    private val geofenceService: com.aliminder.app.domain.service.GeofenceService,
    private val locationService: com.aliminder.app.domain.service.LocationService
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = userSettingsRepository.getUserSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    private val _currentLocation = kotlinx.coroutines.flow.MutableStateFlow<android.location.Location?>(null)
    val currentLocation: StateFlow<android.location.Location?> = _currentLocation

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            val location = locationService.getLastKnownLocation()
            _currentLocation.value = location
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

    fun setHomeAddress(address: com.aliminder.app.domain.model.Address) {
        viewModelScope.launch {
            userSettingsRepository.setHomeAddress(address)
            // Update geofences (use new home, existing work)
            geofenceService.setupGeofences(
                homeAddress = address,
                workAddress = userSettings.value.workAddress
            )
        }
    }

    fun setWorkAddress(address: com.aliminder.app.domain.model.Address) {
        viewModelScope.launch {
            userSettingsRepository.setWorkAddress(address)
            // Update geofences (use existing home, new work)
            geofenceService.setupGeofences(
                homeAddress = userSettings.value.homeAddress,
                workAddress = address
            )
        }
    }

    fun resetMockData() {
        viewModelScope.launch {
            dutyRepository.deleteAllDuties()
            val newEntities = com.aliminder.app.data.local.RealisticMockDataGenerator.generate(java.time.LocalDate.now())
            val newDuties = newEntities.map { it.toDomainDuty() }
            dutyRepository.insertAll(newDuties)
            
            
            // Trigger auto-hide immediately so past duties don't flash on screen
            // StateFlow .value is safe here since we started it in viewModelScope
            dutyRepository.autoHideOverdueDuties(userSettings.value.autoHideOverdueMinutes)
        }
    }
}
