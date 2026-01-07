package com.aliminder.app.data.service

import com.google.gson.annotations.SerializedName

/**
 * Request body for Google Routes API v2.
 */
data class ComputeRoutesRequest(
    @SerializedName("origin") val origin: RouteLocation,
    @SerializedName("destination") val destination: RouteLocation,
    @SerializedName("travelMode") val travelMode: String = "DRIVE",
    @SerializedName("routingPreference") val routingPreference: String = "TRAFFIC_AWARE",
    @SerializedName("computeAlternativeRoutes") val computeAlternativeRoutes: Boolean = false
)

data class RouteLocation(
    @SerializedName("location") val location: LocationPoint? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("vehicleStopover") val vehicleStopover: Boolean? = null
)

data class LocationPoint(
    @SerializedName("latLng") val latLng: LatLng,
    @SerializedName("heading") val heading: Int? = null // Optional bearing (0-360)
)

data class LatLng(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

/**
 * Response body for Google Routes API v2.
 */
data class ComputeRoutesResponse(
    @SerializedName("routes") val routes: List<Route>?
)

data class Route(
    @SerializedName("duration") val duration: String?, // Format: "123s"
    @SerializedName("distanceMeters") val distanceMeters: Int?,
    @SerializedName("staticDuration") val staticDuration: String?
)
