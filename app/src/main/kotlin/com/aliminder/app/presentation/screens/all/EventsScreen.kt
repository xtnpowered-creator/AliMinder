package com.aliminder.app.presentation.screens.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.components.AliMinderTopAppBar
import com.aliminder.app.presentation.components.DismissalDialog
import com.aliminder.app.presentation.components.DutyCard
import com.aliminder.app.presentation.screens.settings.SettingsViewModel

/**
 * Events Screen
 * Shows filtered events (non-tasks) sorted by PoNR proximity
 */
@Composable
fun EventsScreen(
    viewModel: AllViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val allDuties by viewModel.duties.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter for Events: Not SHADOW_TASK AND Not Pending
    val filteredEvents = allDuties.filter { 
        it.category != "SHADOW_TASK" && it.category != "Pending" 
    }

    EventsScreenContent(
        events = filteredEvents,
        overallStage = overallStage,
        useDynamicColor = userSettings.useDynamicTitleBarColor,
        onDismissDuty = viewModel::dismissDuty
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreenContent(
    events: List<Duty>,
    overallStage: PersonaStage,
    useDynamicColor: Boolean,
    onDismissDuty: (Duty, DismissalReason) -> Unit = { _, _ -> }
) {
    var dutyToDismiss by remember { mutableStateOf<Duty?>(null) }

    Scaffold(
        topBar = {
            AliMinderTopAppBar(
                title = "Upcoming Events",
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
            items(events, key = { it.id }) { event ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.StartToEnd) {
                            dutyToDismiss = event
                            // Don't dismiss yet, wait for dialog
                            return@rememberSwipeToDismissBoxState false 
                        }
                        false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                // Transparent background to avoid colored corners behind rounded card
                                .background(Color.Transparent) 
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Only show text/icon if user is swiping far enough, or just always show but no background
                            // Since background is transparent, text might float. 
                            // However, requirement was just to fix corners. 
                            // If we want "visual" indicator, we can put a rounded background here matching the card?
                            // Or just keep it transparent. The user knows they are swiping.
                            // Let's keep it transparent as requested.
                        }
                    },
                    content = {
                        DutyCard(duty = event)
                    }
                )
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

    // Dismissal Dialog
    if (dutyToDismiss != null) {
        DismissalDialog(
            duty = dutyToDismiss!!,
            onDismissRequest = { dutyToDismiss = null },
            onConfirm = { reason ->
                onDismissDuty(dutyToDismiss!!, reason)
                dutyToDismiss = null
            }
        )
    }
}
