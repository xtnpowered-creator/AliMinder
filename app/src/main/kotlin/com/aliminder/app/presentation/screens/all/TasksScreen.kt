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
 * Tasks Screen
 * Shows filtered tasks sorted by PoNR proximity
 */
@Composable
fun TasksScreen(
    viewModel: AllViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val events by viewModel.events.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter for Tasks (Only category "SHADOW_TASK")
    val filteredEvents = events.filter { it.category == "SHADOW_TASK" } // Updated category name

    TasksScreenContent(
        events = filteredEvents,
        overallStage = overallStage,
        useDynamicColor = userSettings.useDynamicTitleBarColor
    )
}

@Composable
fun TasksScreenContent(
    events: List<Event>,
    overallStage: PersonaStage,
    useDynamicColor: Boolean
) {
    Scaffold(
        topBar = {
            AliMinderTopAppBar(
                title = "Upcoming Tasks",
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
                                text = "No tasks scheduled",
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
