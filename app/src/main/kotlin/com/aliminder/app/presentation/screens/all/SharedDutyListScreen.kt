package com.aliminder.app.presentation.screens.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Address
import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.components.AliMinderTopAppBar
import com.aliminder.app.presentation.components.DismissalDialog
import com.aliminder.app.presentation.components.DutyCard
import com.aliminder.app.presentation.components.DutyDetailModal

/**
 * Shared content composable for displaying a list of duties.
 * Handles:
 * - Scaffold & TopAppBar
 * - LazyColumn with Loading (null) and Empty states
 * - Swipe to Dismiss logic
 * - Detail Modal logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedDutyListContent(
    title: String,
    duties: List<Duty>?, // Null = Loading
    emptyStateMessage: String,
    homeAddress: Address?,
    workAddress: Address?,
    onDismissDuty: (Duty, DismissalReason) -> Unit,
    onSetStructuredLocation: (String, Address) -> Unit = { _, _ -> },
    onAcceptDuty: (String) -> Unit = {},
    onDenyDuty: (String) -> Unit = {},
    onToggleChecklistItem: (String, String) -> Unit = { _, _ -> } // Default no-op for now
) {
    var dutyToDismiss by remember { mutableStateOf<Duty?>(null) }
    var selectedDuty by remember { mutableStateOf<Duty?>(null) }

    Scaffold(
        topBar = {
            AliMinderTopAppBar(
                title = title
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
            
            if (duties == null) {
                // Loading State - Render nothing to avoid flash
            } else {
                // Duty List
                items(duties, key = { it.id }) { duty ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.StartToEnd) {
                                dutyToDismiss = duty
                                return@rememberSwipeToDismissBoxState false 
                            }
                            false
                        },
                        positionalThreshold = { it * 0.75f } 
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
                            ) { } // Empty content
                        },
                        content = {
                            DutyCard(
                                duty = duty,
                                onCardClick = { selectedDuty = it }
                            )
                        }
                    )
                }

                // Empty State
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
                                    text = emptyStateMessage,
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
    
    // Duty Detail Modal
    selectedDuty?.let { currentSelection ->
        // Use fresh data from the list if available, otherwise use the stored snapshot
        val liveDuty = duties?.find { it.id == currentSelection.id } ?: currentSelection
        
        DutyDetailModal(
            duty = liveDuty,
            homeAddress = homeAddress,
            workAddress = workAddress,
            onSetStructuredLocation = onSetStructuredLocation,
            onAcceptDuty = onAcceptDuty,
            onDenyDuty = onDenyDuty,
            onToggleChecklistItem = onToggleChecklistItem,
            onDismiss = { selectedDuty = null }
        )
    }
}
