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
    
    private val api: RoutesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://routes.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoutesApi::class.java)
    }
    
    /**
     * Result of a travel time calculation.
     */
    data class TravelResult(
        val minutes: Int,
        val distanceMeters: Int
    )

    private data class CacheEntry(
        val result: TravelResult,
        val timestamp: Long
    )
    
    // In-memory cache: Key -> Entry
    private val cache = java.util.concurrent.ConcurrentHashMap<String, CacheEntry>()
    
    // Throttle maps
    private val lastCallTime = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private val lastResultMap = java.util.concurrent.ConcurrentHashMap<String, TravelResult>()

    /**
     * Calculate travel time and distance from origin to destination.
     */
    suspend fun calculateTravelTime(
        destination: String,
        origin: String,
        heading: Double? = null
    ): TravelResult? {
        
        // 1. Generate Cache Key (Required for both checking and saving)
        val originKey = try {
            val parts = origin.split(",")
            if (parts.size == 2) {
                val lat = String.format("%.3f", parts[0].toDouble())
                val lng = String.format("%.3f", parts[1].toDouble())
                "$lat,$lng"
            } else origin
        } catch (e: Exception) { origin }
        
        val key = "$originKey|$destination"

        // 2. GLOBAL THROTTLE & CACHE CHECK
        val lastTime = lastCallTime[destination] ?: 0L
        val timeSinceLastCall = System.currentTimeMillis() - lastTime
        
        // A. Spatial Cache Check (Fastest)
        val cached = cache[key]
        if (cached != null) {
            val age = System.currentTimeMillis() - cached.timestamp
            if (age < CACHE_TTL_MS) {
                Log.v(TAG, "Cache HIT (Spatial) for '$destination' (Age: ${age/1000}s)")
                return cached.result
            } else {
                 cache.remove(key) // Expired
            }
        }
        
        // B. Time Throttle Check (If we missed spatial, but called recently)
        if (timeSinceLastCall < MIN_INTERVAL_MS) {
             val lastResult = lastResultMap[destination]
             if (lastResult != null) {
                 Log.v(TAG, "Throttle HIT (Time): Returning <60s old result for '$destination'")
                 return lastResult
             }
        }

        // 3. API Call (Cache Miss)
        return try {
            val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
            
            // Parse origin coordinates properly
            val originParts = origin.split(",")
            val originLocation = if (originParts.size == 2) {
                try {
                    val lat = originParts[0].trim().toDouble()
                    val lng = originParts[1].trim().toDouble()
                    RouteLocation(
                        location = LocationPoint(
                            latLng = LatLng(lat, lng),
                            heading = heading?.toInt()
                        )
                    )
                } catch (e: NumberFormatException) {
                    RouteLocation(address = origin)
                }
            } else {
                RouteLocation(address = origin)
            }

            val request = ComputeRoutesRequest(
                origin = originLocation,
                destination = RouteLocation(address = destination),
                travelMode = "DRIVE",
                routingPreference = "TRAFFIC_AWARE"
            )

            Log.d(TAG, "Calling Routes API (MISS): origin=$originLocation, dest='$destination'")

            val response = api.computeRoutes(request = request, apiKey = apiKey)
            
            val route = response.routes?.firstOrNull() ?: return null
            
            val durationString = route.duration
            val durationSeconds = durationString?.trimEnd('s')?.toLongOrNull() ?: return null
            val minutes = ((durationSeconds + 59) / 60).toInt()
            val distanceMeters = route.distanceMeters ?: 0
            
            val result = TravelResult(minutes, distanceMeters)
            
            // 4. Update Caches
            cache[key] = CacheEntry(result, System.currentTimeMillis())
            lastCallTime[destination] = System.currentTimeMillis()
            lastResultMap[destination] = result
            
            Log.d(TAG, "Travel result cached: $minutes min / $distanceMeters meters")
            
            return result
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating travel time", e)
            null
        }
    }
    
    companion object {
        private const val TAG = "GoogleMapsTravelTime"
        private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 Minutes
        private const val MIN_INTERVAL_MS = 60 * 1000L // 1 Minute Hard Throttle
    }
}
