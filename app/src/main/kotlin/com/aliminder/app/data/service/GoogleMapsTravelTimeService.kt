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
     * Calculate travel time in minutes from origin to destination.
     * 
     * @param destination Destination address
     * @param origin Origin coordinates format "lat,lng"
     * @param heading Optional bearing/heading in degrees (0-360)
     * @return Travel time in minutes, or null if API call fails
     */
    suspend fun calculateTravelTime(
        destination: String,
        origin: String,
        heading: Double? = null
    ): Int? {
        return try {
            val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
            
            // Parse origin coordinates
            val originParts = origin.split(",")
            val originLocation = if (originParts.size == 2) {
                try {
                    val lat = originParts[0].trim().toDouble()
                    val lng = originParts[1].trim().toDouble()
                    // Create LocationPoint with optional heading
                    RouteLocation(
                        location = LocationPoint(
                            latLng = LatLng(lat, lng),
                            heading = heading?.toInt()
                        )
                    )
                } catch (e: NumberFormatException) {
                    Log.w(TAG, "Invalid origin format '$origin'. formatting as address.")
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

            Log.d(TAG, "Calling Routes API: origin=$originLocation, dest='$destination'")

            val response = api.computeRoutes(
                request = request,
                apiKey = apiKey
            )
            
            val route = response.routes?.firstOrNull()
            if (route == null) {
                Log.e(TAG, "Routes API returned no routes")
                return null
            }
            
            // Duration comes as "1234s"
            val durationString = route.duration // Traffic aware duration
            val durationSeconds = durationString?.trimEnd('s')?.toLongOrNull()
            
            if (durationSeconds == null) {
                Log.e(TAG, "Invalid duration format: $durationString")
                return null
            }
            
            // Convert seconds to minutes (round up)
            val minutes = ((durationSeconds + 59) / 60).toInt()
            
            Log.d(TAG, "Travel time calculated: $minutes minutes ($durationString)")
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
