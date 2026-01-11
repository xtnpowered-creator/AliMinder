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
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliminder.app.domain.model.Address
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.getCardEligibility
import com.aliminder.app.domain.model.PersonaStage
import com.aliminder.app.presentation.mock.MockData
import java.time.Duration
import java.time.LocalDateTime

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
    onSetStructuredLocation: (String, Address) -> Unit,
    onAcceptDuty: (String) -> Unit,
    onDenyDuty: (String) -> Unit,
    onToggleChecklistItem: (String, String) -> Unit, // New callback
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
                                .verticalScroll(rememberScrollState())
                                // Padding removed here to allow header to be edge-to-edge (has internal padding)
                        ) {
                            // Shared Header (Consistent with DutyCard)
                            DutyCardHeader(
                                duty = duty
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Virtual Meeting Display
                            if (duty.virtualMeetingLink != null) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    // Anchor Button (Matches Squircle Width)
                                    AlignmentButton(
                                        text = "JOIN",
                                        onClick = { /* TODO: Launch intent */ }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Text(
                                        text = duty.virtualMeetingLink,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                        // Padding removed as requested
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Location if set
                            val displayLocation = duty.structuredLocation?.toDisplayString() 
                                ?: duty.location?.let { Address.parse(it).toDisplayString() }

                            if (displayLocation != null) {
                                Row(
                                    verticalAlignment = Alignment.Top, // Align top so first line is consistent
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    // Anchor Button (Matches Squircle Width)
                                    AlignmentButton(
                                        text = "MAP",
                                        onClick = { /* TODO: Launch map intent */ }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    Text(
                                        text = displayLocation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                        // Padding removed as requested
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // Description / Body
                            if (!duty.description.isNullOrBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "DETAILS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF81D4FA), // Light Blue
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HtmlText(
                                        html = duty.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // --- RICH DETAILS ORCHESTRATION ---
                            // Display Organizer, Attendees, Checklist *below* Description

                            val organizer = duty.organizer
                            // Filter out organizer from attendees list to avoid duplication if the model includes them in both
                            val distinctAttendees = duty.attendees.filter { !it.isOrganizer && it.name != organizer?.name }

                            if (organizer != null) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    OrganizerSection(organizer = organizer)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (distinctAttendees.isNotEmpty()) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    AttendeesSection(attendees = distinctAttendees)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (duty.checklist.isNotEmpty()) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    ChecklistSection(
                                        checklist = duty.checklist,
                                        onToggleItem = { itemId -> 
                                            onToggleChecklistItem(duty.id, itemId) 
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // PoNR Calculation breakdown
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                PoNRMathCard(duty = duty)
                            }
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
                                        onSetStructuredLocation = onSetStructuredLocation,
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
    // Address entry dialog (Now using Google Places)
    if (showAddressEntry) {
        AddressEntryModal(
            title = addressEntryTitle,
            contextText = addressEntryContext,
            // initialAddress = null, // TODO: Pass current address if editing
            onSave = { address ->
                when (addressEntryType) {
                    "home" -> {
                        settingsViewModel.setHomeAddress(address)
                        // Also apply to current duty
                        onSetStructuredLocation(duty.id, address)
                    }
                    "work" -> {
                        settingsViewModel.setWorkAddress(address)
                        // Also apply to current duty
                        onSetStructuredLocation(duty.id, address)
                    }
                    "other" -> {
                        // Apply to duty
                        onSetStructuredLocation(duty.id, address)
                    }
                }
                showAddressEntry = false
                onDismiss() // Dismiss modal after setting location
            },
            onDismiss = { showAddressEntry = false }
        )
    }
}

@Composable
private fun AlignmentButton(
    text: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .size(width = 70.dp, height = 32.dp), // Fixed width matches StatusRing (size 48.dp * 1.452 scaling factor ≈ 70.dp)
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp) // Reset padding to fit text
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

