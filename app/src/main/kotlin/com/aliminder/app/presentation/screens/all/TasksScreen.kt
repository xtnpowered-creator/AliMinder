package com.aliminder.app.presentation.screens.all

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.Event
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.components.EventCard
import com.aliminder.app.presentation.theme.BorderDark
import com.aliminder.app.presentation.theme.TextSecondary
import com.aliminder.app.presentation.theme.aliMinderTopAppBarColors

/**
 * Tasks Screen
 * Shows filtered tasks sorted by PoNR proximity
 */
@Composable
fun TasksScreen(
    viewModel: AllViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val events by viewModel.events.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()

    // Filter for Tasks (Only category "Task")
    val filteredEvents = events.filter { it.category == "Task" }

    TasksScreenContent(
        events = filteredEvents,
        overallStage = overallStage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreenContent(
    events: List<Event>,
    overallStage: PersonaStage
) {
    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Text(
                                "Upcoming Tasks",
                                fontSize = (MaterialTheme.typography.titleLarge.fontSize.value + 2).sp,
                                textAlign = TextAlign.Center,
                                color = TextSecondary // Updated color to light gray
                            )
                        }
                    },
                    colors = aliMinderTopAppBarColors()
                )
                HorizontalDivider(thickness = 2.dp, color = BorderDark)
            }
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
