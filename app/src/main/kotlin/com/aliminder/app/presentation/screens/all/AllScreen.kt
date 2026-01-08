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
import androidx.compose.material3.Surface
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
import com.aliminder.app.presentation.components.DutyDetailModal
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
    val userSettings by settingsViewModel.userSettings.collectAsState()

    // Filter out Pending invites from the main dashboard
    // If duties is null (loading), result is null.
    val filteredDuties = duties?.filter { it.category != "Pending" }

    AllScreenContent(
        duties = filteredDuties,
        homeAddress = userSettings.homeAddress,
        workAddress = userSettings.workAddress,
        onDismissDuty = viewModel::dismissDuty,
        onSetLocation = viewModel::updateDutyLocation,
        onSetStructuredLocation = viewModel::updateDutyStructuredLocation,
        onAcceptDuty = viewModel::acceptDuty,
        onDenyDuty = viewModel::denyDuty
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllScreenContent(
    duties: List<Duty>?, // Nullable
    homeAddress: com.aliminder.app.domain.model.Address?,
    workAddress: com.aliminder.app.domain.model.Address?,
    onDismissDuty: (Duty, DismissalReason) -> Unit,
    onSetLocation: (String, String) -> Unit = { _, _ -> },
    onSetStructuredLocation: (String, com.aliminder.app.domain.model.Address) -> Unit = { _, _ -> },
    onAcceptDuty: (String) -> Unit = {},
    onDenyDuty: (String) -> Unit = {}
) {
    SharedDutyListContent(
        title = "All Upcoming Duties",
        duties = duties,
        emptyStateMessage = "No duties scheduled",
        homeAddress = homeAddress,
        workAddress = workAddress,
        onDismissDuty = onDismissDuty,
        onSetLocation = onSetLocation,
        onSetStructuredLocation = onSetStructuredLocation,
        onAcceptDuty = onAcceptDuty,
        onDenyDuty = onDenyDuty
    )
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
                    // prepMinutes removed
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
        // previewStage removed

        Surface(color = MaterialTheme.colorScheme.background) {
            AllScreenContent(
                duties = previewDuties,
                homeAddress = null,
                workAddress = null,
                onDismissDuty = { _, _ -> }
            )
        }
    }
}
