package com.aliminder.app.domain.model

import java.time.LocalDateTime

/**
 * PoNR (Point of No Return) calculation result.
 * 
 * Represents the latest moment the user can depart and still arrive on time,
 * accounting for commute, prep/grooming, and buffer time.
 * 
 * Formula: PoNR = EventTime - (CommuteMinutes + PrepMinutes + BufferMinutes)
 */
data class PoNRCalculation(
    /** The event being calculated for */
    val eventId: String,
    
    /** Event start time */
    val eventTime: LocalDateTime,
    
    /** Commute duration in minutes */
    val commuteMinutes: Int,
    
    /** Safety buffer in minutes */
    val bufferMinutes: Int,
    
    /** Calculated PoNR timestamp */
    val ponrTime: LocalDateTime,
    
    /** Delta: minutes until PoNR (negative = late) */
    val deltaMinutes: Int,
    
    /** Current persona stage based on delta */
    /** Current persona stage based on delta */
    val personaStage: PersonaStage,

    /**
     * Data quality of the calculation.
     */
    val dataQuality: PoNRDataQuality = PoNRDataQuality.GOOD
) {
    /**
     * Total preparation time (commute + buffer).
     */
    val totalPrepMinutes: Int
        get() = commuteMinutes + bufferMinutes
    
    /**
     * Whether user is past the Point of No Return.
     */
    val isPastPoNR: Boolean
        get() = deltaMinutes <= 0
    
    /**
     * Whether user is in critical window (< 15 minutes until PoNR).
     */
    val isCritical: Boolean
        get() = deltaMinutes in 1..14
    
    /**
     * Human-readable delta string (e.g., "+45m", "−10m").
     */
    fun deltaString(): String {
        return if (deltaMinutes >= 0) {
            "+${deltaMinutes}m"
        } else {
            "−${Math.abs(deltaMinutes)}m"
        }
    }
}

/**
 * Quality of the data used for PoNR calculation.
 */
enum class PoNRDataQuality {
    /** High accuracy real-time data (GPS) */
    GOOD,
    
    /** Low accuracy real-time data (>100m accuracy) */
    COARSE,
    
    /** Old/Persisted data (No real-time signal) */
    STALE
}
