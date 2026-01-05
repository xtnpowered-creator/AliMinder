package com.aliminder.app.domain.model

/**
 * Determines which attention/suggestion cards should be displayed for a duty.
 * Single source of truth for card eligibility logic.
 * 
 * Usage:
 * ```
 * val eligibility = duty.getCardEligibility()
 * if (eligibility.hasAnyCards) { ... }
 * if (eligibility.hasPendingCard) { PendingInviteCard() }
 * ```
 */
data class CardEligibility(
    /** Show accept/deny card for pending invites/assignments */
    val hasPendingCard: Boolean,
    
    /** Show location suggestion card for duties without addresses */
    val hasLocationCard: Boolean
    
    // Add future cards here as properties, e.g.:
    // val hasDeadlineCard: Boolean,
    // val hasConflictCard: Boolean,
) {
    /** True if ANY card should be displayed */
    val hasAnyCards: Boolean
        get() = hasPendingCard || hasLocationCard
}

/**
 * Calculate which suggestion cards should be shown for this duty.
 */
fun Duty.getCardEligibility(): CardEligibility {
    return CardEligibility(
        hasPendingCard = category == "Pending",
        
        hasLocationCard = location.isNullOrBlank() && (
            category == "Event" || title.mightContainLocation()
        )
        
        // Future cards:
        // hasDeadlineCard = category == "Task" && deadline == null,
    )
}
