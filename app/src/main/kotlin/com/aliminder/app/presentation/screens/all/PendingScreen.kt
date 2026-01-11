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
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter for Pending items
    // Handle null (loading) by returning null
    val pendingDuties = allDuties?.filter { it.category == "Pending" }

    PendingScreenContent(
        pendingDuties = pendingDuties,
        homeAddress = userSettings.homeAddress,
        workAddress = userSettings.workAddress,
        onSetStructuredLocation = viewModel::updateDutyStructuredLocation,
        onAcceptDuty = viewModel::acceptDuty,
        onDenyDuty = viewModel::denyDuty
    )
}

@Composable
fun PendingScreenContent(
    pendingDuties: List<Duty>?, // Nullable
    homeAddress: com.aliminder.app.domain.model.Address?,
    workAddress: com.aliminder.app.domain.model.Address?,
    onSetStructuredLocation: (String, com.aliminder.app.domain.model.Address) -> Unit = { _, _ -> },
    onAcceptDuty: (String) -> Unit = {},
    onDenyDuty: (String) -> Unit = {}
) {
    SharedDutyListContent(
        title = "Pending Invites",
        duties = pendingDuties,
        emptyStateMessage = "No pending invites",
        homeAddress = homeAddress,
        workAddress = workAddress,
        onDismissDuty = { _, _ -> }, // Pending items might not support dismissal directly in this view, or we can add it
        onSetStructuredLocation = onSetStructuredLocation,
        onAcceptDuty = onAcceptDuty,
        onDenyDuty = onDenyDuty
    )
}
