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
    suspend operator fun invoke(
        duty: Duty,
        defaultPrepMinutes: Int,
        defaultBufferMinutes: Int,
        urgencyThresholdMinutes: Int
    ): PoNRCalculation {
        
        // 1. Calculate travel time
        val commuteMinutes = calculateCommuteTime(duty)
        
        // 2. Get prep and buffer (custom or default)
        val prepMinutes = duty.customPrepMinutes ?: defaultPrepMinutes
        val bufferMinutes = duty.customBufferMinutes ?: defaultBufferMinutes
        
        // 3. Calculate PoNR time
        val totalPrepTime = commuteMinutes + prepMinutes + bufferMinutes
        val ponrTime = duty.startTime.minusMinutes(totalPrepTime.toLong())
        
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
                "commute=$commuteMinutes, prep=$prepMinutes, buffer=$bufferMinutes, " +
                "ponr=$ponrTime, delta=$delta, stage=$personaStage")
        
        return PoNRCalculation(
            eventId = duty.id,
            eventTime = duty.startTime,
            commuteMinutes = commuteMinutes,
            prepMinutes = prepMinutes,
            bufferMinutes = bufferMinutes,
            ponrTime = ponrTime,
            deltaMinutes = delta,
            personaStage = personaStage
        )
    }
    
    /**
     * Calculate commute time with smart fallback logic.
     * 
     * Priority:
     * 1. Google Maps API (if physical location exists)
     * 2. Custom override (if user set it)
     * 3. Zero (default)
     */
    private suspend fun calculateCommuteTime(duty: Duty): Int {
        // Custom override always wins
        if (duty.customCommuteMinutes != null) {
            Log.d(TAG, "Using custom commute: ${duty.customCommuteMinutes} min")
            return duty.customCommuteMinutes
        }
        
        // Check if physical travel is required
        if (!duty.requiresPhysicalTravel()) {
            Log.d(TAG, "No physical travel required (virtual/all-day/no location)")
            return 0
        }
        
        // Try to calculate from API
        val apiResult = travelTimeService.calculateTravelTime(
            destination = duty.location!!,
            origin = "current location placeholder" // TODO: Get from user's home/work location
        )
        
        if (apiResult != null) {
            Log.d(TAG, "Using API-calculated travel time: $apiResult min")
            return apiResult
        }
        
        // API failed - default to zero
        Log.d(TAG, "API failed, defaulting to 0 minutes (no travel time)")
        return 0
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
