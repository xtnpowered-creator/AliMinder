package com.aliminder.app.domain.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aliminder.app.domain.repository.DutyRepository
import com.aliminder.app.presentation.service.LocationTrackingService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

/**
 * Worker that proactively starts location tracking when duties are approaching.
 * 
 * Checks for duties with PoNR within the next hour and starts the LocationTrackingService
 * to ensure accurate location data is available before the user needs to leave.
 * 
 * Scheduled to run every 30 minutes.
 */
@HiltWorker
class ProactiveLocationTrackingWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dutyRepository: DutyRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "ProactiveLocationWorker"
        const val WORK_NAME = "proactive_location_tracking"
        
        // Start tracking when PoNR is within 1 hour
        private const val PROACTIVE_WINDOW_HOURS = 1L
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Checking for upcoming duties...")
            
            val duties = dutyRepository.getAllDuties().first()
            val now = LocalDateTime.now()
            val oneHourFromNow = now.plusHours(PROACTIVE_WINDOW_HOURS)
            
            // Find duties with PoNR within the next hour AND a physical location
            // We do not start location tracking for virtual meetings or Tasks without addresses
            val upcomingDuties = duties.filter { duty ->
                val hasLocation = !duty.location.isNullOrBlank()
                val isApproaching = duty.ponr?.ponrTime?.let { ponrTime ->
                    ponrTime.isAfter(now) && ponrTime.isBefore(oneHourFromNow)
                } ?: false
                
                hasLocation && isApproaching
            }
            
            if (upcomingDuties.isNotEmpty()) {
                Log.d(TAG, "Found ${upcomingDuties.size} duties approaching - starting location tracking")
                
                // IMPORTANT: Start tracking even if user is already away from home/work
                // Geofence only triggers on EXIT, so if app opens while user is already out,
                // we need to start tracking manually
                LocationTrackingService.startMonitoring(applicationContext)
            } else {
                Log.d(TAG, "No duties approaching in next hour")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for upcoming duties", e)
            Result.retry()
        }
    }
}
