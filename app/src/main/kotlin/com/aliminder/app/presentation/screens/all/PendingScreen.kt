package com.aliminder.app.presentation.screens.all

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.Event
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.components.AliMinderTopAppBar
import com.aliminder.app.presentation.components.EventCard
import com.aliminder.app.presentation.screens.settings.SettingsViewModel

/**
 * Pending Invites Screen
 * Shows duties to which user has been invited but not yet accepted.
 */
@Composable
fun PendingScreen(
    viewModel: AllViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val events by viewModel.events.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter for Pending items
    val filteredEvents = events.filter { it.category == "Pending" }

    PendingScreenContent(
        events = filteredEvents,
        overallStage = overallStage,
        useDynamicColor = userSettings.useDynamicTitleBarColor
    )
}

@Composable
fun PendingScreenContent(
    events: List<Event>,
    overallStage: PersonaStage,
    useDynamicColor: Boolean
) {
    Scaffold(
        topBar = {
            AliMinderTopAppBar(
                title = "Pending Invites",
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
            items(events) { event ->
                EventCard(event = event)
            }

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
                                text = "No pending invites",
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
