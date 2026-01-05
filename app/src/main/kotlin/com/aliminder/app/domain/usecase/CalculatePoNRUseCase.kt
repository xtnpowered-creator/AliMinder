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
        currentLocation: android.location.Location?,
        userHomeAddress: Address?,
        userWorkAddress: Address?,
        defaultPrepMinutes: Int,
        defaultBufferMinutes: Int,
        urgencyThresholdMinutes: Int
    ): PoNRCalculation {
        
        // 1. Calculate travel time
        val commuteMinutes = calculateCommuteTime(duty, currentLocation, userHomeAddress, userWorkAddress)
        
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
     * 1. Custom override (if user set it)
     * 2. Google Maps API with current GPS location as origin
     * 3. Zero (if no destination or API fails)
     */
    private suspend fun calculateCommuteTime(
        duty: Duty,
        currentLocation: android.location.Location?,
        userHomeAddress: Address?,
        userWorkAddress: Address?
    ): Int {
        // No physical location = no commute needed
        if (duty.location.isNullOrBlank()) {
            return 0
        }

        // Check if custom commute time is set
        if (duty.customCommuteMinutes != null && duty.customCommuteMinutes > 0) {
            return duty.customCommuteMinutes
        }

        // If no current GPS location available, return 0
        if (currentLocation == null) {
            return 0
        }

        // Call Google Maps API with current lat/lng and bearing
        return try {
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
                Log.d(TAG, "Using API-calculated travel time: $apiResult min from GPS (${currentLocation.latitude},${currentLocation.longitude}) with bearing ${bearing ?: "none"} to ${duty.location}")
                apiResult
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating travel time from current location", e)
            0
        }
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
