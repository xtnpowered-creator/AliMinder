package com.aliminder.app.domain.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.aliminder.app.domain.model.TrackingState
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced LocationService with intelligent state machine.
 * 
 * Implements Phase 2 strategy:
 * - Dormant: No tracking (user at home/work)
 * - Monitoring: WiFi/cell updates (5min intervals)
 * - Active: GPS tracking (30sec intervals with strategic batching)
 * 
 * Integrates with:
 * - ActivityRecognitionService: Detects user movement
 * - GeofenceService: Triggers when leaving home/work
 */
@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRecognitionService: ActivityRecognitionService
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _trackingState = MutableStateFlow<TrackingState>(TrackingState.Dormant)
    val trackingState: StateFlow<TrackingState> = _trackingState
    
    // Emit location updates for reactive PoNR recalculation
    private val _locationUpdates = MutableStateFlow<Location?>(null)
    val locationUpdates: StateFlow<Location?> = _locationUpdates
    
    private var lastSignificantLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    
    companion object {
        private const val TAG = "LocationService"
        private const val SIGNIFICANT_DISPLACEMENT_METERS = 200f
    }
    
    /**
     * Start location tracking with activity recognition.
     * Transitions states based on detected activity.
     */
    fun startTracking(initialState: TrackingState = TrackingState.Monitoring) {
        Log.d(TAG, "Starting location tracking in $initialState state")
        transitionTo(initialState)
        
        // Observe activity changes
        scope.launch {
            activityRecognitionService.startTracking()
                .collect { activity ->
                    handleActivityChange(activity)
                }
        }
    }
    
    /**
     * Stop location tracking and activity recognition.
     */
    fun stopTracking() {
        Log.d(TAG, "Stopping location tracking")
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        transitionTo(TrackingState.Dormant)
    }
    
    /**
     * Transition to a new tracking state.
     */
    fun transitionTo(newState: TrackingState) {
        val oldState = _trackingState.value
        
        if (oldState == newState) {
            Log.d(TAG, "Already in $newState state")
            return
        }
        
        Log.d(TAG, "State transition: $oldState → $newState")
        _trackingState.value = newState
        
        // Restart location updates with new state config
        if (newState !is TrackingState.Dormant) {
            restartLocationUpdates(newState)
        } else {
            // Stop location updates
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
        }
    }
    
    /**
     * Handle activity changes from ActivityRecognitionService.
     */
    private fun handleActivityChange(activity: DetectedActivity) {
        val currentState = _trackingState.value
        
        when (activity.type) {
            DetectedActivity.STILL -> {
                // User stopped moving
                if (currentState is TrackingState.Active) {
                    Log.d(TAG, "User STILL detected - transitioning to Monitoring")
                    transitionTo(TrackingState.Monitoring)
                }
            }
            
            DetectedActivity.IN_VEHICLE -> {
                // User driving - high-priority tracking
                if (currentState !is TrackingState.Active) {
                    Log.d(TAG, "IN_VEHICLE detected - transitioning to Active")
                    // Start with null, will be updated by calculateNearestDutyPoNRMinutes
                    transitionTo(TrackingState.Active(minutesToNearestDutyPoNR = null))
                }
            }
            
            DetectedActivity.WALKING, DetectedActivity.RUNNING, DetectedActivity.ON_BICYCLE -> {
                // User moving but not in vehicle
                if (currentState is TrackingState.Dormant) {
                    Log.d(TAG, "${activityTypeToString(activity.type)} detected - transitioning to Monitoring")
                    transitionTo(TrackingState.Monitoring)
                }
            }
        }
    }
    
    /**
     * Restart location updates with new state configuration.
     */
    @SuppressLint("MissingPermission")
    private fun restartLocationUpdates(state: TrackingState) {
        // Remove old callback
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        
        // Create new callback
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                
                // Always emit for PoNR recalculation (DutyRepository listens to this)
                _locationUpdates.value = location
                
                // Check if displacement is significant for logging
                val shouldEmit = lastSignificantLocation?.let { last ->
                    location.distanceTo(last) >= SIGNIFICANT_DISPLACEMENT_METERS
                } ?: true
                
                if (shouldEmit) {
                    lastSignificantLocation = location
                    Log.d(TAG, "Significant location update: lat=${location.latitude}, lng=${location.longitude}, bearing=${if (location.hasBearing()) location.bearing else "none"}")
                } else {
                    Log.v(TAG, "Location update (no significant movement)")
                }
            }
        }
        
        locationCallback = callback
        
        // Create location request based on state
        val locationRequest = createLocationRequest(state)
        
        // Start receiving updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )
        
        Log.d(TAG, "Location updates started with ${state.getUpdateIntervalMs()}ms interval")
    }
    
    /**
     * Create location request based on tracking state.
     */

    private var isAppForeground: Boolean = false // Track app visibility for power optimization

    /**
     * Update foreground state to optimize GPS power usage.
     * High Accuracy is only used when App is Foreground AND State is Active.
     */
    fun setForegroundState(inForeground: Boolean) {
        if (isAppForeground != inForeground) {
            isAppForeground = inForeground
            Log.d(TAG, "App foreground state changed: $inForeground. Restarting updates for power optimization.")
            // Restart updates to apply new priority if needed
            restartLocationUpdates(_trackingState.value)
        }
    }

    /**
     * Create location request based on tracking state.
     */
    private fun createLocationRequest(state: TrackingState): LocationRequest {
        val (shouldBatch, batchDelay) = state.shouldBatch()
        
        // Only use HIGH_ACCURACY if:
        // 1. Moving (Active)
        // 2. User looking at app (Foreground)
        // 3. Imminent Duty (PoNR < 60 min or passed)
        val isUrgent = state is TrackingState.Active && 
                       state.minutesToNearestDutyPoNR != null && 
                       state.minutesToNearestDutyPoNR < 60

        val priority = if (isUrgent && isAppForeground) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        return LocationRequest.Builder(
            priority,
            state.getUpdateIntervalMs()
        ).apply {
            setMinUpdateIntervalMillis(state.getFastestIntervalMs())
            setWaitForAccurateLocation(false)
            
            // Strategic batching for Active state when far from destination
            if (shouldBatch && batchDelay != null) {
                setMaxUpdateDelayMillis(batchDelay)
                Log.d(TAG, "Strategic batching enabled: ${batchDelay}ms delay")
            }
        }.build()
    }
    
    /**
     * Update nearest duty PoNR time for Active state batching decisions.
     * Per LOCATION_PLAN: Batching is based on TIME to PoNR, not distance.
     */
    /**
     * Update nearest duty PoNR time for Active state batching decisions.
     * 
     * CHANGED: This NO LONGER forces a transition to Active state.
     * Active state is strictly for PHYSICAL MOVEMENT (Activity Recognition).
     * This method merely updates the Data Context for batching logic IF we are already Active.
     */
    fun updateNearestDutyPoNRMinutes(minutesToPoNR: Int?) {
        val currentState = _trackingState.value
        
        if (currentState is TrackingState.Active) {
            // We are driving. Update the urgency to adjust batching freq.
            if (currentState.minutesToNearestDutyPoNR != minutesToPoNR) {
                Log.d(TAG, "Updating flow urgency while Active: $minutesToPoNR min")
                transitionTo(TrackingState.Active(minutesToPoNR))
            }
        } else {
            // We are Sitting Still (Monitoring/Dormant).
            // Do NOT switch to Active. Saves battery.
            // We trust the 10-minute Monitoring poll to catch traffic changes.
            Log.v(TAG, "PoNR Calculated ($minutesToPoNR min) but user is not driving. Staying in $currentState.")
        }
    }
    
    /**
     * Gets the current location, actively requesting if needed.
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        return try {
            // First try cached location
            val cachedLocation = fusedLocationClient.lastLocation.await()
            
            if (cachedLocation != null) {
                Log.d(TAG, "Using cached location: lat=${cachedLocation.latitude}, lng=${cachedLocation.longitude}, age=${System.currentTimeMillis() - cachedLocation.time}ms")
                return cachedLocation
            }
            
            // No cache - actively request fresh location
            Log.d(TAG, "No cached location. Requesting fresh location...")
            
            val freshLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await()
            
            if (freshLocation != null) {
                Log.d(TAG, "Got fresh location: lat=${freshLocation.latitude}, lng=${freshLocation.longitude}, accuracy=${freshLocation.accuracy}m")
            } else {
                Log.w(TAG, "Fresh location request returned null. Location services may be disabled.")
            }
            
            freshLocation
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            null
        }
    }
    
    /**
     * Checks if location has moved significantly.
     */
    fun hasMovedSignificantly(newLocation: Location): Boolean {
        return lastSignificantLocation?.let { last ->
            newLocation.distanceTo(last) >= SIGNIFICANT_DISPLACEMENT_METERS
        } ?: true
    }
    
    /**
     * Calculate minutes until nearest upcoming duty's PoNR and update Active state.
     * Used to optimize strategic batching based on actual time urgency.
     * Per LOCATION_PLAN: Batching decisions based on TIME, not distance.
     */
    suspend fun calculateAndUpdateNearestDutyPoNR(dutyRepository: com.aliminder.app.domain.repository.DutyRepository) {
        try {
            val duties = dutyRepository.getAllDuties().first()
            val now = java.time.LocalDateTime.now()
            
            // Find upcoming duties with calculated PoNR and a PHYSICAL location
            // We ignore Virtual meetings/Tasks without addresses for GPS urgency
            val upcomingDuties = duties.filter { duty ->
                duty.ponr != null && 
                duty.ponr.ponrTime.isAfter(now) &&
                !duty.location.isNullOrBlank()
            }
            
            if (upcomingDuties.isEmpty()) {
                Log.d(TAG, "No upcoming duties with PoNR")
                updateNearestDutyPoNRMinutes(null)
                return
            }
            
            // Find the duty with the soonest PoNR
            val nearestDuty = upcomingDuties.minByOrNull { it.ponr!!.deltaMinutes }
            
            if (nearestDuty != null) {
                val minutesToPoNR = nearestDuty.ponr!!.deltaMinutes
                Log.d(TAG, "Nearest duty PoNR: ${minutesToPoNR} minutes (${nearestDuty.title})")
                updateNearestDutyPoNRMinutes(minutesToPoNR)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating nearest duty PoNR", e)
        }
    }
    
    private fun activityTypeToString(type: Int): String = when (type) {
        DetectedActivity.STILL -> "STILL"
        DetectedActivity.WALKING -> "WALKING"
        DetectedActivity.RUNNING -> "RUNNING"
        DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
        DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
        else -> "UNKNOWN"
    }
}
