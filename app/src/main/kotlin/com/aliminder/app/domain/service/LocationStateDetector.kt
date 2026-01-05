package com.aliminder.app.domain.service

import android.location.Location
import com.aliminder.app.domain.model.LocationState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Determines user's movement state from location velocity data.
 * 
 * Uses velocity thresholds per LOCATION_PLAN.md:
 * - < 0.5 m/s: Stationary (sitting, standing)
 * - 0.5-5 m/s: Active (walking, slow bike)
 * - >= 5 m/s: Transit (driving, fast bike, bus)
 */
@Singleton
class LocationStateDetector @Inject constructor() {
    
    companion object {
        private const val STATIONARY_THRESHOLD_MPS = 0.5f
        private const val ACTIVE_THRESHOLD_MPS = 5.0f
    }
    
    /**
     * Determines movement state from a location object.
     * 
     * @param location Location with velocity data
     * @return The detected LocationState
     */
    fun detectState(location: Location): LocationState {
        // Check if velocity data is available
        if (!location.hasSpeed()) {
            // Default to Active if no velocity data (conservative approach)
            return LocationState.Active
        }
        
        val speedMps = location.speed
        
        return when {
            speedMps < STATIONARY_THRESHOLD_MPS -> LocationState.Stationary
            speedMps < ACTIVE_THRESHOLD_MPS -> LocationState.Active
            else -> LocationState.Transit
        }
    }
    
    /**
     * Determines if the state change is significant enough to warrant
     * adjusting update intervals.
     */
    fun isSignificantStateChange(oldState: LocationState, newState: LocationState): Boolean {
        // Any state change is significant for interval adjustment
        return oldState != newState
    }
}
