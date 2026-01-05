package com.aliminder.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.aliminder.app.domain.model.Address
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.getCardEligibility
import com.aliminder.app.domain.model.mightContainLocation

/**
 * Single source of truth for determining which attention cards to show for a duty.
 * Renders all applicable attention cards in priority order.
 */
@Composable
fun DutyAttentionCards(
    duty: Duty,
    homeAddress: Address?,
    workAddress: Address?,
    onSetLocation: (String, String) -> Unit,
    onAcceptDuty: (String) -> Unit,
    onDenyDuty: (String) -> Unit,
    onDismissModal: () -> Unit,
    onShowAddressEntry: (String, String?, String) -> Unit // title, context, type
) {
    // Use eligibility helper for single source of truth
    val eligibility = duty.getCardEligibility()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Priority 1: Pending invite (needs immediate user action)
        if (eligibility.hasPendingCard) {
            PendingInviteCard(
                onAccept = {
                    onAcceptDuty(duty.id)
                    onDismissModal()
                },
                onDeny = {
                    onDenyDuty(duty.id)
                    onDismissModal()
                }
            )
        }
        
        // Priority 2: Missing location (affects PoNR calculation)
        // DEBUG: Log why card is/isn't showing
        android.util.Log.d("DutyAttentionCards", "=== LOCATION CARD DEBUG ===")
        android.util.Log.d("DutyAttentionCards", "Duty: ${duty.title}")
        android.util.Log.d("DutyAttentionCards", "Category: '${duty.category}'")
        android.util.Log.d("DutyAttentionCards", "Location: '${duty.location}'")
        android.util.Log.d("DutyAttentionCards", "Location isNullOrBlank: ${duty.location.isNullOrBlank()}")
        android.util.Log.d("DutyAttentionCards", "Is Event?: ${duty.category == "Event"}")
        android.util.Log.d("DutyAttentionCards", "Might have location keywords?: ${duty.title.mightContainLocation()}")
        android.util.Log.d("DutyAttentionCards", "Should show card?: ${eligibility.hasLocationCard}")
        
        if (eligibility.hasLocationCard) {
            LocationSuggestionCard(
                onSelectHome = {
                    if (homeAddress != null) {
                        onSetLocation(duty.id, homeAddress.toGoogleMapsFormat())
                        onDismissModal()
                    } else {
                        onShowAddressEntry("Enter Home Address", "For: ${duty.title}", "home")
                    }
                },
                onSelectWork = {
                    if (workAddress != null) {
                        onSetLocation(duty.id, workAddress.toGoogleMapsFormat())
                        onDismissModal()
                    } else {
                        onShowAddressEntry("Enter Work Address", "For: ${duty.title}", "work")
                    }
                },
                onSelectOther = {
                    onShowAddressEntry("Enter Location", "For: ${duty.title}", "other")
                }
            )
        }
        
        // Future attention cards can be added here in priority order:
        // - Travel time calculation suggestions
        // - Conflicting duties warnings
        // - Preparation reminders
        // etc.
    }
}
