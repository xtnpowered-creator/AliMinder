package com.aliminder.app.presentation.screens.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliminder.app.domain.model.Event
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.mock.MockData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Unified Sentinel Dashboard (AllScreen).
 *
 * Manages the stream of events sorted by PoNR proximity and the overall vigilance stage.
 * Currently uses MockData until CalendarRepository is implemented.
 */
@HiltViewModel
class AllViewModel @Inject constructor(
    // TODO: Inject CalendarRepository
) : ViewModel() {

    // Internal mutable state for events (Mock data for now)
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    
    // Public immutable state
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    // Derived state for the overall vigilance stage (based on the most urgent event)
    val overallStage: StateFlow<PersonaStage> = _events
        .map { eventList ->
            eventList.minByOrNull { it.delta }?.getPersonaStage() ?: PersonaStage.OPTIMISTIC
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PersonaStage.OPTIMISTIC
        )

    init {
        loadMockData()
    }

    private fun loadMockData() {
        // Transform MockData objects to Domain objects
        val domainEvents = MockData.sampleEvents.map { mockEvent ->
            // Map MockEvent to Domain Event
            // Note: This is a temporary mapping until the Repository layer is fully built
            com.aliminder.app.domain.model.Event(
                id = mockEvent.id,
                title = mockEvent.title,
                startTime = mockEvent.startTime,
                endTime = mockEvent.startTime.plusHours(1), // Mock duration
                provider = com.aliminder.app.domain.model.EventProvider.SHADOW, // Assume Shadow for mock
                category = mockEvent.category,
                // We're cheating a bit here by calculating delta in the domain model mapping
                // In real app, CalculatePoNRUseCase would handle this
                delta = mockEvent.deltaMinutes.toInt(),
                ponr = com.aliminder.app.domain.model.PoNRCalculation(
                    eventId = mockEvent.id,
                    eventTime = mockEvent.startTime,
                    commuteMinutes = mockEvent.commuteMinutes,
                    prepMinutes = mockEvent.prepMinutes,
                    bufferMinutes = mockEvent.bufferMinutes,
                    ponrTime = mockEvent.ponr,
                    deltaMinutes = mockEvent.deltaMinutes.toInt(),
                    personaStage = mockEvent.personaStage.let { stage ->
                        // Map Presentation Mock PersonaStage to Domain PersonaStage
                        // They happen to have same enum names but different packages potentially
                        // Actually, MockData imports presentation.mock.PersonaStage
                        // We need domain.model.PersonaStage
                        when(stage) {
                            com.aliminder.app.presentation.mock.PersonaStage.OPTIMISTIC -> PersonaStage.OPTIMISTIC
                            com.aliminder.app.presentation.mock.PersonaStage.WEARY -> PersonaStage.WEARY
                            com.aliminder.app.presentation.mock.PersonaStage.GRAVE -> PersonaStage.GRAVE
                        }
                    }
                )
            )
        }.sortedBy { it.delta }

        _events.value = domainEvents
    }
}
