package com.aliminder.app.presentation.screens.all

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.components.AliMinderTopAppBar
import com.aliminder.app.presentation.components.DutyCard
import com.aliminder.app.presentation.components.DutyDetailModal
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
    val allDuties by viewModel.duties.collectAsState()
    val overallStage by viewModel.overallStage.collectAsState()
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter for Pending items
    val pendingDuties = allDuties.filter { it.category == "Pending" }

    PendingScreenContent(
        pendingDuties = pendingDuties,
        overallStage = overallStage,
        useDynamicColor = userSettings.useDynamicTitleBarColor,
        homeAddress = userSettings.homeAddress,
        workAddress = userSettings.workAddress,
        onSetLocation = viewModel::updateDutyLocation,
        onAcceptDuty = viewModel::acceptDuty,
        onDenyDuty = viewModel::denyDuty
    )
}

@Composable
fun PendingScreenContent(
    pendingDuties: List<Duty>,
    overallStage: PersonaStage,
    useDynamicColor: Boolean,
    homeAddress: com.aliminder.app.domain.model.Address?,
    workAddress: com.aliminder.app.domain.model.Address?,
    onSetLocation: (String, String) -> Unit = { _, _ -> },
    onAcceptDuty: (String) -> Unit = {},
    onDenyDuty: (String) -> Unit = {}
) {
    var selectedDuty by remember { mutableStateOf<Duty?>(null) }
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
            items(pendingDuties) { pendingDuty ->
                DutyCard(
                    duty = pendingDuty,
                    onCardClick = { selectedDuty = it }
                )
            }

            if (pendingDuties.isEmpty()) {
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
    
    // Duty Detail Modal
    selectedDuty?.let { duty ->
        DutyDetailModal(
            duty = duty,
            homeAddress = homeAddress,
            workAddress = workAddress,
            onSetLocation = onSetLocation,
            onAcceptDuty = onAcceptDuty,
            onDenyDuty = onDenyDuty,
            onDismiss = { selectedDuty = null }
        )
    }
}
