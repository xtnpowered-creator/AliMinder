package com.aliminder.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.repository.DutyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestoreViewModel @Inject constructor(
    private val dutyRepository: DutyRepository
) : ViewModel() {

    val dismissedDuties: StateFlow<List<Duty>> = dutyRepository.getAllDuties()
        .map { allDuties -> allDuties.filter { it.isDismissed } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun restoreDuty(duty: Duty) {
        viewModelScope.launch {
            dutyRepository.restoreDuty(duty.id)
        }
    }
}
