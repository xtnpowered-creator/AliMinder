package com.aliminder.app.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.presentation.components.DutyCard
import com.aliminder.app.presentation.components.DutyDetailModal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreScreen(
    viewModel: RestoreViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val dismissedDuties by viewModel.dismissedDuties.collectAsState()
    var dutyToRestore by remember { mutableStateOf<Duty?>(null) }
    var selectedDuty by remember { mutableStateOf<Duty?>(null) }

    // Show confirmation dialog when a duty is selected for restoration
    if (dutyToRestore != null) {
        RestoreDialog(
            onConfirm = {
                viewModel.restoreDuty(dutyToRestore!!)
                dutyToRestore = null
            },
            onDismiss = { dutyToRestore = null }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dismissedDuties, key = { it.id }) { duty ->
            SwipeToRestoreDutyCard(
                duty = duty,
                onRestore = { dutyToRestore = duty }, // Trigger the dialog
                onCardClick = { selectedDuty = it }
            )
        }

        if (dismissedDuties.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No dismissed duties found.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
    
    // Duty Detail Modal (no accept/deny for dismissed duties)
    selectedDuty?.let { duty ->
        DutyDetailModal(
            duty = duty,
            homeAddress = null,
            workAddress = null,
            onSetLocation = { _, _ -> },
            onAcceptDuty = {},
            onDenyDuty = {},
            onDismiss = { selectedDuty = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRestoreDutyCard(
    duty: Duty,
    onRestore: () -> Unit,
    onCardClick: (Duty) -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onRestore()
                // Return false to prevent the swipe from dismissing the item immediately.
                // The item will be removed from the list when the state updates.
                return@rememberSwipeToDismissBoxState false 
            }
            false
        },
        positionalThreshold = { it * 0.75f } // Require 75% swipe distance
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Restore", color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        },
        content = {
            DutyCard(
                duty = duty,
                onCardClick = onCardClick
            )
        }
    )
}

@Composable
private fun RestoreDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Duty") },
        text = { Text("Do you want to restore this duty?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Restore")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
