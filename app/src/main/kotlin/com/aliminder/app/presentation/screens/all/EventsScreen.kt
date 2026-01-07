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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.aliminder.app.presentation.components.DutyDetailModal
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
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter for Events: Not Task AND Not Pending
    // Handle null (loading) by returning null
    val filteredEvents = allDuties?.filter { 
        it.category != "Task" && it.category != "Pending" 
    }

    EventsScreenContent(
        events = filteredEvents,
        homeAddress = userSettings.homeAddress,
        workAddress = userSettings.workAddress,
        onDismissDuty = viewModel::dismissDuty,
        onSetLocation = viewModel::updateDutyLocation,
        onAcceptDuty = viewModel::acceptDuty,
        onDenyDuty = viewModel::denyDuty
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreenContent(
    events: List<Duty>?, // Nullable
    homeAddress: com.aliminder.app.domain.model.Address?,
    workAddress: com.aliminder.app.domain.model.Address?,
    onDismissDuty: (Duty, DismissalReason) -> Unit = { _, _ -> },
    onSetLocation: (String, String) -> Unit = { _, _ -> },
    onAcceptDuty: (String) -> Unit = {},
    onDenyDuty: (String) -> Unit = {}
) {
    SharedDutyListContent(
        title = "Upcoming Events",
        duties = events,
        emptyStateMessage = "No events scheduled",
        homeAddress = homeAddress,
        workAddress = workAddress,
        onDismissDuty = onDismissDuty,
        onSetLocation = onSetLocation,
        onAcceptDuty = onAcceptDuty,
        onDenyDuty = onDenyDuty
    )
}
