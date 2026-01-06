package com.aliminder.app.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Address
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.getCardEligibility
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.domain.model.needsAttention
import com.aliminder.app.presentation.mock.MockData
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.abs

/**
 * Expanded duty card modal - appears as large card with margins.
 * Top section: Duty information (tappable to dismiss)
 * Bottom section: Scrollable attention/action section
 */
@Composable
fun DutyDetailModal(
    duty: Duty,
    homeAddress: Address?,
    workAddress: Address?,
    onSetLocation: (String, String) -> Unit,
    onAcceptDuty: (String) -> Unit,
    onDenyDuty: (String) -> Unit,
    onDismiss: () -> Unit,
    settingsViewModel: com.aliminder.app.presentation.screens.settings.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var showAddressEntry by remember { mutableStateOf(false) }
    var addressEntryTitle by remember { mutableStateOf("") }
    var addressEntryContext by remember { mutableStateOf<String?>(null) }
    var addressEntryType by remember { mutableStateOf("") } // "home", "work", or "other"
    
    // DEBUG: Verify modal is rendering
    android.util.Log.d("DutyDetailModal", "=== MODAL OPENED ===")
    android.util.Log.d("DutyDetailModal", "Duty: ${duty.title}")
    android.util.Log.d("DutyDetailModal", "Category: ${duty.category}")
    android.util.Log.d("DutyDetailModal", "Location: ${duty.location}")
    
    // Handle back button
    BackHandler(enabled = true) {
        onDismiss()
    }
    
    // Force recomposition every minute to update countdown and color
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000) // 1 minute
            currentTime = LocalDateTime.now()
        }
    }
    
    val stage = duty.getPersonaStage()
    val deltaValue = Duration.between(currentTime, duty.startTime).toMinutes()
    
    // Blue background (app background color)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Card containing both sections with consistent margins
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // All sides get same margin
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = CardDefaults.shape
        ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val maxHeight = maxHeight
                    
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top Section: Scrollable duty details
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Takes available space
                                .clickable { onDismiss() } // Tap anywhere in details to dismiss
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            // Header with countdown and title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatusRing(
                                    stage = stage,
                                    deltaText = formatEventDelta(deltaValue, stage),
                                    size = 60.dp,
                                    strokeWidth = 5.dp
                                )
                                
                                Column {
                                    Text(
                                        text = duty.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    val isTask = duty.category?.contains("Task", ignoreCase = true) == true
                                    val timeLabel = if (isTask) "Due" else "Start"
                                    
                                    Text(
                                        text = "$timeLabel: ${MockData.formatTime(duty.startTime)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Location if set
                            if (duty.location != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = duty.location,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // PoNR Calculation breakdown
                            PoNRMathCard(duty = duty)
                        }
                        
                        // Bottom Section: Attention area - only if any cards qualify
                        // Use eligibility helper for single source of truth
                        val eligibility = duty.getCardEligibility()
                        
                        if (eligibility.hasAnyCards) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = maxHeight * 0.5f) // Max 50% of modal
                                    .padding(16.dp)
                            ) {
                                AttentionSection {
                                    DutyAttentionCards(
                                        duty = duty,
                                        homeAddress = homeAddress,
                                        workAddress = workAddress,
                                        onSetLocation = onSetLocation,
                                        onAcceptDuty = onAcceptDuty,
                                        onDenyDuty = onDenyDuty,
                                        onDismissModal = onDismiss,
                                        onShowAddressEntry = { title, context, type ->
                                            addressEntryTitle = title
                                            addressEntryContext = context
                                            addressEntryType = type
                                            showAddressEntry = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }    
    // Address entry dialog
    if (showAddressEntry) {
        AddressEntryDialog(
            title = addressEntryTitle,
            contextText = addressEntryContext,
            onSave = { address ->
                when (addressEntryType) {
                    "home" -> {
                        // Save to user settings for future use
                        settingsViewModel.setHomeAddress(address)
                        // Also apply to current duty
                        onSetLocation(duty.id, address.toGoogleMapsFormat())
                    }
                    "work" -> {
                        // Save to user settings for future use
                        settingsViewModel.setWorkAddress(address)
                        // Also apply to current duty
                        onSetLocation(duty.id, address.toGoogleMapsFormat())
                    }
                    "other" -> {
                        // Only apply to current duty (don't save to settings)
                        onSetLocation(duty.id, address.toGoogleMapsFormat())
                    }
                }
                showAddressEntry = false
                onDismiss() // Dismiss modal after setting location
            },
            onDismiss = { showAddressEntry = false }
        )
    }
}

private fun formatEventDelta(minutes: Long, stage: PersonaStage): String {
    val absMinutes = abs(minutes)
    val days = absMinutes / (24 * 60)
    val remainingMinutes = absMinutes % (24 * 60)
    val hours = remainingMinutes / 60
    val mins = remainingMinutes % 60

    if (stage == PersonaStage.LATE) {
        return "LATE"
    }
    
    if (stage == PersonaStage.URGENT) {
        return String.format(Locale.US, "%02d:%02d", hours, mins)
    }

    return if (minutes >= 0) {
        if (days > 0) {
            val dayLabel = if (days == 1L) "day" else "days"
            String.format(Locale.US, "%d %s\n%02d:%02d", days, dayLabel, hours, mins)
        } else {
            String.format(Locale.US, "%02d:%02d", hours, mins)
        }
    } else {
        "LATE"
    }
}
