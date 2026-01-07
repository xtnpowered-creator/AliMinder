package com.aliminder.app.data.service

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for Google Routes API v2.
 * Base URL: https://routes.googleapis.com/
 */
interface RoutesApi {
    
    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Body request: ComputeRoutesRequest,
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = "routes.duration,routes.distanceMeters,routes.staticDuration"
    ): ComputeRoutesResponse
}
