package com.aliminder.app.presentation.screens.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliminder.app.data.local.DatabaseInitializer
import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.domain.repository.DutyRepository
import com.aliminder.app.domain.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllViewModel @Inject constructor(
    private val dutyRepository: DutyRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val databaseInitializer: DatabaseInitializer
) : ViewModel() {

    // Main dashboard shows only non-dismissed duties
    // Main dashboard shows only non-dismissed duties
    // Null means "Loading", Empty means "No duties"
    val duties: StateFlow<List<Duty>?> = dutyRepository.getAllDuties()
        .map { allDuties -> allDuties.filter { !it.isDismissed } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val overallStage: StateFlow<PersonaStage> = duties
        .map { dutyList ->
            dutyList?.minByOrNull { it.delta }?.getPersonaStage() ?: PersonaStage.OPTIMISTIC
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PersonaStage.OPTIMISTIC
        )

    init {
        // Seed the database on first launch
        viewModelScope.launch {
            databaseInitializer.initialize()
            
            // Auto-hide overdue duties on startup
            val settings = userSettingsRepository.getUserSettings().first()
            dutyRepository.autoHideOverdueDuties(settings.autoHideOverdueMinutes)
        }
    }
    
    fun dismissDuty(duty: Duty, reason: DismissalReason) {
        viewModelScope.launch {
            dutyRepository.dismissDuty(duty.id, reason)
        }
    }

    fun updateDutyLocation(dutyId: String, location: String) {
        viewModelScope.launch {
            dutyRepository.updateDutyLocation(dutyId, location)
        }
    }
    
    fun updateDutyStructuredLocation(dutyId: String, address: com.aliminder.app.domain.model.Address) {
        viewModelScope.launch {
            dutyRepository.updateDutyStructuredLocation(dutyId, address)
        }
    }

    fun updateDutyCustomCommute(dutyId: String, commuteMinutes: Int) {
        viewModelScope.launch {
            dutyRepository.updateDutyCustomCommute(dutyId, commuteMinutes)
        }
    }
    
    fun acceptDuty(dutyId: String) {
        viewModelScope.launch {
            dutyRepository.acceptDuty(dutyId)
        }
    }
    
    fun denyDuty(dutyId: String) {
        viewModelScope.launch {
            dutyRepository.denyDuty(dutyId)
        }
    }
}
