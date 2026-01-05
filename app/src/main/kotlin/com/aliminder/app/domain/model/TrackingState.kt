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
    fun getUpdateIntervalMs(): Long = when (this) {
        is Dormant -> Long.MAX_VALUE // No updates
        is Monitoring -> 3 * 60 * 1000L // 3 minutes (user spec: >30min from PoNR)
        is Active -> 30 * 1000L // 30 seconds (per LOCATION_PLAN_REFINEMENTS.md)
    }
    
    /**
     * Get minimum interval between location updates in milliseconds.
     */
    fun getFastestIntervalMs(): Long = when (this) {
        is Dormant -> Long.MAX_VALUE
        is Monitoring -> 60 * 1000L // 1 minute
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
                // No duty info yet, don't batch
                false to null
            } else if (minutesToNearestDutyPoNR > 15) {
                // Far away: batch updates for 5 minutes
                true to 5 * 60 * 1000L
            } else if (minutesToNearestDutyPoNR <= 8) {
                // Critical zone: no batching, real-time
                false to null
            } else {
                // Medium zone (8-15 min): moderate batching (2 min)
                true to 2 * 60 * 1000L
            }
        }
    }
}
