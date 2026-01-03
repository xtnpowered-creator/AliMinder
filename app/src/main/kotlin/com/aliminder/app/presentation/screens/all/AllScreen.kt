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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.DutyProvider
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.domain.model.PoNRCalculation
import com.aliminder.app.presentation.components.AliMinderTopAppBar
import com.aliminder.app.presentation.components.DismissalDialog
import com.aliminder.app.presentation.components.DutyCard
import com.aliminder.app.presentation.mock.MockData
import com.aliminder.app.presentation.screens.settings.SettingsViewModel
import com.aliminder.app.presentation.theme.AliMinderTheme

/**
 * ALL Screen - Unified Sentinel Dashboard
 * Shows all duties sorted by PoNR proximity
 */
@Composable
fun AllScreen(
    viewModel: AllViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val duties by viewModel.duties.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter out Pending invites from the main dashboard
    val filteredDuties = duties.filter { it.category != "Pending" }

    AllScreenContent(
        duties = filteredDuties,
        overallStage = overallStage,
        useDynamicColor = userSettings.useDynamicTitleBarColor,
        onDismissDuty = viewModel::dismissDuty
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllScreenContent(
    duties: List<Duty>,
    overallStage: PersonaStage,
    useDynamicColor: Boolean,
    onDismissDuty: (Duty, DismissalReason) -> Unit
) {
    var dutyToDismiss by remember { mutableStateOf<Duty?>(null) }

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
            // Duty list
            items(duties, key = { it.id }) { duty ->
                // Swipe to dismiss
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.StartToEnd) {
                            dutyToDismiss = duty
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
                                .background(Color.Transparent)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                           // Transparent background
                        }
                    },
                    content = {
                        DutyCard(duty = duty)
                    }
                )
            }

            // Empty state (if list is empty)
            if (duties.isEmpty()) {
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
                                text = "No duties scheduled",
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

@Preview(name = "AllScreen Preview", showBackground = true)
@Composable
fun AllScreenPreview() {
    AliMinderTheme {
        // Map MockData to Domain Duty for Preview
        val previewDuties = MockData.sampleEvents.map { mockEvent ->
             Duty(
                id = mockEvent.id,
                title = mockEvent.title,
                startTime = mockEvent.startTime,
                endTime = mockEvent.startTime.plusHours(1),
                provider = DutyProvider.SHADOW,
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
                        com.aliminder.app.presentation.mock.PersonaStage.GRAVE -> PersonaStage.URGENT // Updated
                    }
                ),
                // Fix: isDismissed is computed now, so remove it from constructor.
                // However, dismissalReason is optional and defaults to null which maps to isDismissed=false.
                // We can just rely on default.
                dismissalReason = null
            )
        }.sortedBy { it.delta }

        // Determine stage for preview
        val previewStage = previewDuties.firstOrNull()?.getPersonaStage() ?: PersonaStage.OPTIMISTIC

        Surface(color = MaterialTheme.colorScheme.background) {
            AllScreenContent(
                duties = previewDuties,
                overallStage = previewStage,
                useDynamicColor = true,
                onDismissDuty = { _, _ -> }
            )
        }
    }
}
