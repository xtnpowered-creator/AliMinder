package com.aliminder.app.data.service

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Google Maps Distance Matrix API.
 */
interface DistanceMatrixApi {
    
    @GET("distancematrix/json")
    suspend fun getDistanceMatrix(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("mode") mode: String = "driving",
        @Query("departure_time") departureTime: Long? = null,
        @Query("traffic_model") trafficModel: String? = "best_guess",
        @Query("key") apiKey: String
    ): DistanceMatrixResponse
}
