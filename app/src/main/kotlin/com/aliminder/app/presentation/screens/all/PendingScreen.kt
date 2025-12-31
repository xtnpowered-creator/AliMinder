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
 * Pending Invites Screen
 * Shows duties to which user has been invited but not yet accepted.
 */
@Composable
fun PendingScreen(
    viewModel: AllViewModel = hiltViewModel()
) {
    // Observe state from ViewModel
    val events by viewModel.events.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()

    // Filter for Pending items
    val filteredEvents = events.filter { it.category == "Pending" }

    PendingScreenContent(
        events = filteredEvents,
        overallStage = overallStage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingScreenContent(
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
                                "Pending Invites",
                                fontSize = (MaterialTheme.typography.titleLarge.fontSize.value + 2).sp,
                                textAlign = TextAlign.Center,
                                color = TextSecondary
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
