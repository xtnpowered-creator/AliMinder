package com.aliminder.app.domain.model

/**
 * Travel modes supported by Google Maps Distance Matrix API.
 */
enum class TravelMode {
    DRIVING,
    TRANSIT,
    WALKING,
    BICYCLING;
    
    fun toApiParam(): String = name.lowercase()
}
