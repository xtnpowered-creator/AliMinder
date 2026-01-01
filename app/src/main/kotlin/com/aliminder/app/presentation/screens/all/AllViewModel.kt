package com.aliminder.app.presentation.screens.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliminder.app.data.local.DatabaseInitializer
import com.aliminder.app.domain.model.Event
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.domain.repository.DutyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllViewModel @Inject constructor(
    private val dutyRepository: DutyRepository,
    private val databaseInitializer: DatabaseInitializer
) : ViewModel() {

    val events: StateFlow<List<Event>> = dutyRepository.getAllDuties()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val overallStage: StateFlow<PersonaStage> = events
        .map { eventList ->
            eventList.minByOrNull { it.delta }?.getPersonaStage() ?: PersonaStage.OPTIMISTIC
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
        }
    }
}
