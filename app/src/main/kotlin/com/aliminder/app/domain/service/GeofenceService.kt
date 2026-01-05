package com.aliminder.app.domain.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.util.Log
import com.aliminder.app.domain.model.Address
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing geofences around home/work addresses.
 * Triggers location tracking only when user exits these "safe zones".
 */
@Singleton
class GeofenceService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    
    companion object {
        private const val TAG = "GeofenceService"
        const val HOME_GEOFENCE_ID = "home_geofence"
        const val WORK_GEOFENCE_ID = "work_geofence"
        const val GEOFENCE_RADIUS_METERS = 200f
        private const val ACTION_GEOFENCE_EVENT = "com.aliminder.app.GEOFENCE_EVENT"
    }
    
    /**
     * Set up geofences around home and/or work addresses.
     * Clears existing geofences first.
     */
    @SuppressLint("MissingPermission")
    suspend fun setupGeofences(
        homeAddress: Address?,
        workAddress: Address?
    ): Result<Unit> {
        return try {
            // Remove existing geofences
            removeGeofences()
            
            val geofences = mutableListOf<Geofence>()
            
            // Add home geofence
            homeAddress?.let { address ->
                geocodeAddress(address)?.let { latLng ->
                    geofences.add(createGeofence(HOME_GEOFENCE_ID, latLng.first, latLng.second))
                    Log.d(TAG, "Created home geofence at ${latLng.first}, ${latLng.second}")
                }
            }
            
            // Add work geofence
            workAddress?.let { address ->
                geocodeAddress(address)?.let { latLng ->
                    geofences.add(createGeofence(WORK_GEOFENCE_ID, latLng.first, latLng.second))
                    Log.d(TAG, "Created work geofence at ${latLng.first}, ${latLng.second}")
                }
            }
            
            if (geofences.isEmpty()) {
                Log.d(TAG, "No geofences to add")
                return Result.success(Unit)
            }
            
            // Add geofences
            geofencingClient.addGeofences(
                createGeofencingRequest(geofences),
                geofencePendingIntent
            ).await()
            
            Log.d(TAG, "Successfully added ${geofences.size} geofence(s)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup geofences", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove all geofences.
     */
    suspend fun removeGeofences() {
        try {
            geofencingClient.removeGeofences(geofencePendingIntent).await()
            Log.d(TAG, "Removed all geofences")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove geofences", e)
        }
    }
    
    /**
     * Create a circular geofence.
     */
    private fun createGeofence(id: String, latitude: Double, longitude: Double): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT) // Only care about exits
            .setLoiteringDelay(60_000) // 1 minute before considering "exit"
            .build()
    }
    
    /**
     * Create geofencing request.
     */
    private fun createGeofencingRequest(geofences: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofences(geofences)
        }.build()
    }
    
    /**
     * Geocode an Address to lat/lng coordinates.
     * Returns null if geocoding fails.
     */
    private fun geocodeAddress(address: Address): Pair<Double, Double>? {
        return try {
            val addressString = address.toGoogleMapsFormat()
            
            val results = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ - use new async API synchronously
                var resultList: List<android.location.Address>? = null
                geocoder.getFromLocationName(addressString, 1) { addresses ->
                    resultList = addresses
                }
                // Wait a bit for callback (geocoding is fast)
                Thread.sleep(100)
                resultList
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(addressString, 1)
            }
            
            if (results != null && results.isNotEmpty()) {
                val location = results[0]
                Pair(location.latitude, location.longitude)
            } else {
                Log.w(TAG, "Geocoding returned no results for: $addressString")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocoding failed for address: ${address.toGoogleMapsFormat()}", e)
            null
        }
    }
    
    /**
     * PendingIntent for geofence transitions.
     */
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(ACTION_GEOFENCE_EVENT)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}

/**
 * BroadcastReceiver for geofence events.
 * Handles geofence transitions (ENTER/EXIT).
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "GeofenceBroadcastRx"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent == null) {
            Log.e(TAG, "Geofencing event is null")
            return
        }
        
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing event error: ${geofencingEvent.errorCode}")
            return
        }
        
        val transition = geofencingEvent.geofenceTransition
        
        when (transition) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                triggeringGeofences?.forEach { geofence ->
                    Log.d(TAG, "User exited geofence: ${geofence.requestId}")
                }
                
                // Start location tracking service
                com.aliminder.app.presentation.service.LocationTrackingService.startMonitoring(context)
            }
            
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                val triggeringGeofences = geofencingEvent.triggeringGeofences
                triggeringGeofences?.forEach { geofence ->
                    Log.d(TAG, "User entered geofence: ${geofence.requestId}")
                }
                
                // Check if we should stop tracking (no duties in next 2 hours)
                checkAndStopTracking(context)
            }
        }
    }
    
    /**
     * Check for upcoming duties and stop tracking if none in next 2 hours.
     */
    private fun checkAndStopTracking(context: Context) {
        // Use coroutine to check duties asynchronously
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val dutyRepository = dagger.hilt.android.EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    DutyRepositoryEntryPoint::class.java
                ).dutyRepository()
                
                val duties = dutyRepository.getAllDuties().first()
                val now = java.time.LocalDateTime.now()
                val twoHoursFromNow = now.plusHours(2)
                
                // Check if any duties have PoNR within next 2 hours
                val hasUpcomingDuties = duties.any { duty ->
                    duty.ponr?.ponrTime?.let { ponrTime ->
                        ponrTime.isAfter(now) && ponrTime.isBefore(twoHoursFromNow)
                    } ?: false
                }
                
                if (hasUpcomingDuties) {
                    Log.d(TAG, "Continued tracking - duties upcoming in next 2 hours")
                } else {
                    Log.d(TAG, "Stopping tracking - no duties in next 2 hours")
                    com.aliminder.app.presentation.service.LocationTrackingService.stopMonitoring(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking duties", e)
                // On error, keep tracking to be safe
            }
        }
    }
}

/**
 * Entry point for accessing DutyRepository from BroadcastReceiver.
 */
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
@dagger.hilt.EntryPoint
interface DutyRepositoryEntryPoint {
    fun dutyRepository(): com.aliminder.app.domain.repository.DutyRepository
}
