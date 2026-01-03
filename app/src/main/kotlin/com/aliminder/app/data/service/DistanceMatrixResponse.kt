package com.aliminder.app.data.service

import com.google.gson.annotations.SerializedName

/**
 * Response model for Google Maps Distance Matrix API.
 * 
 * Documentation: https://developers.google.com/maps/documentation/distance-matrix/overview
 */
data class DistanceMatrixResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("rows")
    val rows: List<Row>
) {
    data class Row(
        @SerializedName("elements")
        val elements: List<Element>
    )
    
    data class Element(
        @SerializedName("status")
        val status: String,
        
        @SerializedName("duration")
        val duration: Duration?,
        
        @SerializedName("duration_in_traffic")
        val durationInTraffic: Duration?,
        
        @SerializedName("distance")
        val distance: Distance?
    )
    
    data class Duration(
        @SerializedName("value")
        val valueSeconds: Int,  // Duration in seconds
        
        @SerializedName("text")
        val text: String  // Human-readable duration (e.g., "23 mins")
    )
    
    data class Distance(
        @SerializedName("value")
        val valueMeters: Int,  // Distance in meters
        
        @SerializedName("text")
        val text: String  // Human-readable distance (e.g., "15.2 km")
    )
}
