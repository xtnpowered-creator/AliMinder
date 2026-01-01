package com.aliminder.app.presentation.screens.all

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.Event
import com.aliminder.app.domain.model.EventProvider
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.domain.model.PoNRCalculation
import com.aliminder.app.presentation.components.AliMinderTopAppBar
import com.aliminder.app.presentation.components.EventCard
import com.aliminder.app.presentation.mock.MockData
import com.aliminder.app.presentation.screens.settings.SettingsViewModel
import com.aliminder.app.presentation.theme.AliMinderTheme

/**
 * ALL Screen - Unified Sentinel Dashboard
 * Shows all events sorted by PoNR proximity
 */
@Composable
fun AllScreen(
    viewModel: AllViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val events by viewModel.events.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    AllScreenContent(
        events = events,
        overallStage = overallStage,
        useDynamicColor = userSettings.useDynamicTitleBarColor
    )
}

@Composable
fun AllScreenContent(
    events: List<Event>,
    overallStage: PersonaStage,
    useDynamicColor: Boolean
) {
    Scaffold(
        topBar = {
            AliMinderTopAppBar(
                title = "All Upcoming Duties",
                overallStage = overallStage,
                useDynamicColor = useDynamicColor
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Event list
            items(events) { event ->
                EventCard(event = event)
            }

            // Empty state (if list is empty)
            if (events.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No events scheduled",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "AllScreen Preview", showBackground = true)
@Composable
fun AllScreenPreview() {
    AliMinderTheme {
        // Map MockData to Domain Event for Preview
        val previewEvents = MockData.sampleEvents.map { mockEvent ->
             Event(
                id = mockEvent.id,
                title = mockEvent.title,
                startTime = mockEvent.startTime,
                endTime = mockEvent.startTime.plusHours(1),
                provider = EventProvider.SHADOW,
                category = mockEvent.category,
                delta = mockEvent.deltaMinutes.toInt(),
                ponr = PoNRCalculation(
                    eventId = mockEvent.id,
                    eventTime = mockEvent.startTime,
                    commuteMinutes = mockEvent.commuteMinutes,
                    prepMinutes = mockEvent.prepMinutes,
                    bufferMinutes = mockEvent.bufferMinutes,
                    ponrTime = mockEvent.ponr,
                    deltaMinutes = mockEvent.deltaMinutes.toInt(),
                    personaStage = when(mockEvent.personaStage) {
                        com.aliminder.app.presentation.mock.PersonaStage.OPTIMISTIC -> PersonaStage.OPTIMISTIC
                        com.aliminder.app.presentation.mock.PersonaStage.WEARY -> PersonaStage.WEARY
                        com.aliminder.app.presentation.mock.PersonaStage.GRAVE -> PersonaStage.GRAVE
                    }
                )
            )
        }.sortedBy { it.delta }

        // Determine stage for preview
        val previewStage = previewEvents.firstOrNull()?.getPersonaStage() ?: PersonaStage.OPTIMISTIC

        Surface(color = MaterialTheme.colorScheme.background) {
            AllScreenContent(
                events = previewEvents,
                overallStage = previewStage,
                useDynamicColor = true
            )
        }
    }
}
