package com.aliminder.app.domain.model

/**
 * Represents the location tracking state of the application.
 * Used to determine update frequency and power consumption.
 */
sealed class TrackingState {
    /**
     * No location tracking. User is at home/work within geofence.
     * Battery impact: None
     */
    object Dormant : TrackingState()
    
    /**
     * Monitoring user location using WiFi/cell towers.
     * User has left home/work geofence but is not actively moving.
     * Battery impact: Very Low (3min intervals per user spec)
     */
    object Monitoring : TrackingState()
    
    /**
     * Active GPS tracking. User is moving (IN_VEHICLE/WALKING).
     * 
     * @param minutesToNearestDutyPoNR Minutes until the nearest duty's Point of No Return.
     *        Used for strategic batching decisions per LOCATION_PLAN_REFINEMENTS.md.
     * Battery impact: Low to Medium (30sec intervals, batched when >15min away)
     */
    data class Active(
        val minutesToNearestDutyPoNR: Int?
    ) : TrackingState()
    
    /**
     * Get location update interval in milliseconds for this state.
     */
    /**
     * Get location update interval in milliseconds for this state.
     */
    fun getUpdateIntervalMs(): Long = when (this) {
        is Dormant -> Long.MAX_VALUE // No updates
        is Monitoring -> 10 * 60 * 1000L // 10 minutes (Relaxed "Pocket Vigilance")
        is Active -> 30 * 1000L // 30 seconds (Driving)
    }
    
    /**
     * Get minimum interval between location updates in milliseconds.
     */
    fun getFastestIntervalMs(): Long = when (this) {
        is Dormant -> Long.MAX_VALUE
        is Monitoring -> 5 * 60 * 1000L // 5 minutes
        is Active -> 10 * 1000L // 10 seconds
    }
    
    /**
     * Determine if strategic batching should be enabled.
     * Per LOCATION_PLAN_REFINEMENTS.md:
     * - Far (>15 min to PoNR): Batch with 5-min delay
     * - Critical (<8 min to PoNR): No batching, real-time updates
     */
    fun shouldBatch(): Pair<Boolean, Long?> = when (this) {
        is Dormant, is Monitoring -> false to null
        is Active -> {
            if (minutesToNearestDutyPoNR == null) {
                // FAIL-SAFE: If urgency is unknown, do not assume urgent.
                // Just use standard active tracking.
                true to 150 * 1000L 
            } else if (minutesToNearestDutyPoNR > 30) {
                // Far away (>30 min): Batch updates for 5 minutes
                true to 5 * 60 * 1000L
            } else if (minutesToNearestDutyPoNR <= 20) {
                // Critical zone (<20 min): No batching, real-time
                false to null
            } else {
                // Medium zone (20-30 min):
                // User requirement: Max 2.5 min refresh (150s)
                true to 150 * 1000L
            }
        }
    }
}
