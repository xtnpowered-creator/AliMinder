package com.aliminder.app.data.service

import android.util.Log
import com.aliminder.app.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for calculating travel time using Google Maps Distance Matrix API.
 */
@Singleton
class GoogleMapsTravelTimeService @Inject constructor() {
    
    private val api: DistanceMatrixApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DistanceMatrixApi::class.java)
    }
    
    /**
     * Calculate travel time in minutes from origin to destination.
     * 
     * @param destination Destination address or place name
     * @param origin Origin address (default: current location placeholder)
     * @param heading Optional bearing/heading in degrees (0-360) to prevent wrong-side-of-road errors
     * @return Travel time in minutes, or null if API call fails
     */
    suspend fun calculateTravelTime(
        destination: String,
        origin: String = "current+location",
        heading: Double? = null
    ): Int? {
        return try {
            // Log API key (first/last 4 chars only for security)
            val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
            val keyPreview = if (apiKey.length > 8) {
                "${apiKey.take(4)}...${apiKey.takeLast(4)}"
            } else {
                "INVALID_OR_EMPTY"
            }
            Log.d(TAG, "API Key loaded: $keyPreview")
            Log.d(TAG, "Calling API: origin='$origin', destination='$destination', heading=${heading ?: "none"}")
            
            val response = api.getDistanceMatrix(
                origins = origin,
                destinations = destination,
                mode = "driving",
                departureTime = null, // Not using for now
                trafficModel = null,  // Can't use without departureTime
                apiKey = apiKey
            )
            
            Log.d(TAG, "API Response status: ${response.status}")
            
            // Check API response status
            if (response.status != "OK") {
                Log.e(TAG, "Distance Matrix API error: ${response.status}")
                return null
            }
            
            // Extract duration from response
            val element = response.rows.firstOrNull()?.elements?.firstOrNull()
            if (element?.status != "OK") {
                Log.e(TAG, "Element status error: ${element?.status}")
                return null
            }
            
            // Prefer duration_in_traffic if available, otherwise use duration
            val durationSeconds = element.durationInTraffic?.valueSeconds 
                ?: element.duration?.valueSeconds
            
            if (durationSeconds == null) {
                Log.e(TAG, "No duration data in response")
                return null
            }
            
            // Convert seconds to minutes (round up)
            val minutes = (durationSeconds + 59) / 60
            
            Log.d(TAG, "Travel time calculated: $minutes minutes from '$origin' to '$destination'")
            return minutes
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating travel time", e)
            null
        }
    }
    
    companion object {
        private const val TAG = "GoogleMapsTravelTime"
    }
}
