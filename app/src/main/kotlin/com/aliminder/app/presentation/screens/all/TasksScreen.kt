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
 * Tasks Screen
 * Shows filtered tasks sorted by PoNR proximity
 */
@Composable
fun TasksScreen(
    viewModel: AllViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val allDuties by viewModel.duties.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter for Tasks (Only category "SHADOW_TASK")
    val tasks = allDuties.filter { it.category == "SHADOW_TASK" } // Updated category name

    TasksScreenContent(
        tasks = tasks,
        overallStage = overallStage,
        useDynamicColor = userSettings.useDynamicTitleBarColor,
        onDismissDuty = viewModel::dismissDuty
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreenContent(
    tasks: List<Duty>,
    overallStage: PersonaStage,
    useDynamicColor: Boolean,
    onDismissDuty: (Duty, DismissalReason) -> Unit = { _, _ -> }
) {
    var dutyToDismiss by remember { mutableStateOf<Duty?>(null) }

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
            // Task list
            items(tasks, key = { it.id }) { task ->
                 val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.StartToEnd) {
                            dutyToDismiss = task
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
                            // Background is transparent to fix the corner issue
                        }
                    },
                    content = {
                        DutyCard(duty = task)
                    }
                )
            }

            // Empty state (if list is empty)
            if (tasks.isEmpty()) {
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
