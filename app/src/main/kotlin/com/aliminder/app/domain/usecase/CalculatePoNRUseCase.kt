package com.aliminder.app.domain.usecase

import android.util.Log
import com.aliminder.app.data.service.GoogleMapsTravelTimeService
import com.aliminder.app.domain.model.*
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use Case for calculating Point of No Return (PoNR) for a duty.
 * 
 * Formula: PoNR = Event Start Time - (Travel Time + Prep Time + Buffer Time)
 * 
 * Travel Time Priority:
 * 1. Google Maps API (if physical location exists)
 * 2. Custom commute minutes (if user set override)
 * 3. Zero (default - no travel time)
 */
class CalculatePoNRUseCase @Inject constructor(
    private val travelTimeService: GoogleMapsTravelTimeService
) {
    
    /**
     * Calculate PoNR for a duty with given user settings.
     */
    /**
     * Calculate PoNR for a duty with given user settings.
     */
    /**
     * Calculate PoNR for a duty with given user settings.
     */
    suspend operator fun invoke(
        duty: Duty,
        currentLocation: android.location.Location?,
        defaultBufferMinutes: Int,
        urgencyThresholdMinutes: Int
    ): PoNRCalculation {
        
        // Optimize: If it's a generic "Task" (not an event with a location), assume 0 travel time
        // unless it explicitly has a location set.
        val isTask = duty.category?.contains("Task", ignoreCase = true) == true
        val skipTravelCalc = isTask && duty.location.isNullOrBlank()

        // 1. Calculate travel time & determine quality
        val (commuteMinutes, dataQuality) = if (skipTravelCalc) {
            0 to PoNRDataQuality.GOOD // No travel needed, so it's "Good" (definitive)
        } else {
            calculateCommuteTime(duty, currentLocation)
        }
        
        // 2. Get buffer (custom or default)
        val bufferMinutes = duty.customBufferMinutes ?: defaultBufferMinutes
        
        // 3. Calculate PoNR time
        val totalDeduction = commuteMinutes + bufferMinutes
        val ponrTime = duty.startTime.minusMinutes(totalDeduction.toLong())
        
        // 4. Calculate delta (minutes until PoNR)
        val now = LocalDateTime.now()
        val delta = Duration.between(now, ponrTime).toMinutes().toInt()
        
        // 5. Determine persona stage
        val personaStage = determinePersonaStage(
            delta = delta,
            startTime = duty.startTime,
            now = now,
            urgencyThreshold = urgencyThresholdMinutes
        )
        
        Log.d(TAG, "PoNR calculated for '${duty.title}': " +
                "commute=$commuteMinutes ($dataQuality), buffer=$bufferMinutes, " +
                "ponr=$ponrTime, delta=$delta, stage=$personaStage")
        
        return PoNRCalculation(
            eventId = duty.id,
            eventTime = duty.startTime,
            commuteMinutes = commuteMinutes,
            bufferMinutes = bufferMinutes,
            ponrTime = ponrTime,
            deltaMinutes = delta,
            personaStage = personaStage,
            dataQuality = dataQuality
        )
    }
    
    /**
     * Calculate commute time with smart fallback logic.
     * Returns a Pair: (CommuteMinutes, DataQuality)
     * 
     * Priority:
     * 1. Custom override (Good)
     * 2. Fresh GPS/Network Calculation (Good/Coarse)
     * 3. Fallback to Persisted Value (Stale)
     * 4. Zero (Stale/Default)
     */
    private suspend fun calculateCommuteTime(
        duty: Duty,
        currentLocation: android.location.Location?
    ): Pair<Int, PoNRDataQuality> {
        // No physical location = no commute needed
        if (duty.location.isNullOrBlank()) {
            return 0 to PoNRDataQuality.GOOD
        }

        // Custom override is always considered "Good" (User intent)
        if (duty.customCommuteMinutes != null && duty.customCommuteMinutes > 0) {
            return duty.customCommuteMinutes to PoNRDataQuality.GOOD
        }

        // --- Try Fresh Calculation ---
        if (currentLocation != null) {
            // Determine accuracy quality
            val isCoarse = currentLocation.accuracy > 100 // >100m is considered coarse
            val quality = if (isCoarse) PoNRDataQuality.COARSE else PoNRDataQuality.GOOD
            
            try {
                val origin = "${currentLocation.latitude},${currentLocation.longitude}"
                val bearing = if (currentLocation.hasBearing()) {
                    currentLocation.bearing.toDouble()
                } else {
                    null
                }
                
                val apiResult = travelTimeService.calculateTravelTime(
                    destination = duty.location,
                    origin = origin,
                    heading = bearing
                )
                
                if (apiResult != null && apiResult > 0) {
                    Log.d(TAG, "Fresh API travel time: $apiResult min via ${if(isCoarse) "Coarse" else "Fine"} loc")
                    return apiResult to quality
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fresh travel calculation failed", e)
                // Continue to fallback
            }
        }

        // --- Fallback to Persisted Data ---
        if (duty.lastCalculatedCommuteMinutes != null && duty.lastCalculatedCommuteMinutes > 0) {
            Log.w(TAG, "Using STALE persisted travel time: ${duty.lastCalculatedCommuteMinutes} min")
            return duty.lastCalculatedCommuteMinutes to PoNRDataQuality.STALE
        }

        // --- Last Resort ---
        // We have no location, no API result, and no history.
        // We must return 0, but flag it as STALE so the user knows it's unverified.
        return 0 to PoNRDataQuality.STALE
    }
    /**
     * Determine persona stage based on delta and start time.
     */
    private fun determinePersonaStage(
        delta: Int,
        startTime: LocalDateTime,
        now: LocalDateTime,
        urgencyThreshold: Int
    ): PersonaStage {
        return when {
            now.isAfter(startTime) -> PersonaStage.LATE
            delta > urgencyThreshold -> PersonaStage.OPTIMISTIC
            delta in 0..urgencyThreshold -> PersonaStage.WEARY
            else -> PersonaStage.URGENT  // Past PoNR but before start time
        }
    }
    
    companion object {
        private const val TAG = "CalculatePoNRUseCase"
    }
}
