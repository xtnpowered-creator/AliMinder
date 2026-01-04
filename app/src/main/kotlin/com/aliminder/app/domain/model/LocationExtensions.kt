package com.aliminder.app.domain.model

/**
 * Extension functions for location-related logic.
 */

/**
 * Check if a location string represents a virtual meeting.
 */
fun String?.isVirtualMeeting(): Boolean {
    if (this.isNullOrBlank()) return false
    
    val virtualIndicators = listOf(
        "http://", "https://",  // URLs
        "teams.microsoft.com",
        "zoom.us",
        "meet.google.com",
        "webex.com",
        "zoom", "teams", "skype", "meet", "webex", "call"
    )
    
    return virtualIndicators.any { this.contains(it, ignoreCase = true) }
}

/**
 * Check if a duty requires physical travel.
 * Returns false for virtual meetings, null/blank locations, or all-day events.
 */
fun Duty.requiresPhysicalTravel(): Boolean {
    return when {
        isAllDay -> false
        location.isNullOrBlank() -> false
        location.isVirtualMeeting() -> false
        else -> true
    }
}

/**
 * Check if a task/duty title suggests it might involve a physical location.
 * Used to suggest adding commute time or location to duties without location data.
 */
fun String.mightContainLocation(): Boolean {
    val locationKeywords = listOf(
        " at ", " @ ",
        "go to", "going to",
        "meet ", "meeting ",
        "lunch", "dinner", "coffee", "breakfast",
        "restaurant", "cafe", "bar",
        "appointment", "visit"
    )
    return locationKeywords.any { this.contains(it, ignoreCase = true) }
}

/**
 * Check if duty needs user attention/action.
 * Returns true if:
 * 1. Title suggests location but no location field AND no custom commute set
 * 2. Has physical location but no custom commute (future: suggest API calculation)
 */
fun Duty.needsAttention(): Boolean {
    // Title mentions location keywords but has no location field or custom commute
    if (location.isNullOrBlank() && 
        title.mightContainLocation() && 
        customCommuteMinutes == null) {
        return true
    }
    
    return false
}

/**
 * Get the reason why this duty needs attention.
 * Returns null if duty doesn't need attention.
 */
fun Duty.getAttentionReason(): String? {
    if (!needsAttention()) return null
    
    return when {
        location.isNullOrBlank() && title.mightContainLocation() -> {
            // Find which keyword triggered this
            val detectedKeyword = listOf(
                " at ", " @ ", "go to", "going to", "meet ", "meeting ",
                "lunch", "dinner", "coffee", "breakfast",
                "restaurant", "cafe", "bar", "appointment", "visit"
            ).find { title.contains(it, ignoreCase = true) }?.trim()
            
            if (detectedKeyword != null) {
                "This task mentions \"$detectedKeyword\" but doesn't have travel time set."
            } else {
                "This task might need travel time."
            }
        }
        else -> null
    }
}
