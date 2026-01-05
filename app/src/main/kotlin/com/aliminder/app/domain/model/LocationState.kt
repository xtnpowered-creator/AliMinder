package com.aliminder.app.domain.model

/**
 * Represents the user's current movement state based on velocity.
 * Used to determine location update frequency for battery optimization.
 * 
 * Per LOCATION_PLAN.md:
 * - Stationary: 5 min updates, <1% battery/hour
 * - Active: 1 min updates, ~2-3% battery/hour  
 * - Transit: 30 sec updates, ~5-8% battery/hour
 */
sealed class LocationState {
    /**
     * User is stationary (e.g., at desk, in meeting).
     * Velocity < 0.5 m/s
     * Update interval: 5 minutes
     */
    data object Stationary : LocationState()
    
    /**
     * User is actively moving (e.g., walking, grooming).
     * Velocity: 0.5 - 5 m/s
     * Update interval: 1 minute
     */
    data object Active : LocationState()
    
    /**
     * User is in transit (e.g., driving, riding).
     * Velocity >= 5 m/s
     * Update interval: 30 seconds
     */
    data object Transit : LocationState()
    
    /**
     * Returns the update interval in milliseconds for this state.
     */
    fun getUpdateIntervalMs(): Long = when (this) {
        Stationary -> 5 * 60 * 1000L  // 5 minutes
        Active -> 1 * 60 * 1000L       // 1 minute
        Transit -> 30 * 1000L          // 30 seconds
    }
    
    /**
     * Returns the fastest interval (allows hitchhiking on other apps).
     */
    fun getFastestIntervalMs(): Long = getUpdateIntervalMs() / 2
}
