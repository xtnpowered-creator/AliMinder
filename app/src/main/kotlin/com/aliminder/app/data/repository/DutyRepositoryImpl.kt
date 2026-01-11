package com.aliminder.app.data.repository

import android.util.Log
import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.mapper.toDomainDuty
import com.aliminder.app.data.mapper.toDutyEntity
import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.repository.DutyRepository
import com.aliminder.app.domain.usecase.CalculatePoNRUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.aliminder.app.domain.service.LocationService
import com.aliminder.app.data.service.GoogleMapsTravelTimeService

import kotlinx.coroutines.CoroutineScope
import com.aliminder.app.di.ApplicationScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Singleton
class DutyRepositoryImpl @Inject constructor(
    private val dutyDao: DutyDao,
    private val userSettingsDao: UserSettingsDao,
    private val userSettingsRepository: com.aliminder.app.domain.repository.UserSettingsRepository,
    private val locationService: LocationService,
    private val calculatePoNRUseCase: CalculatePoNRUseCase,
    private val googleMapsTravelTimeService: GoogleMapsTravelTimeService, // For direct usage if needed

    @ApplicationScope private val scope: CoroutineScope
) : DutyRepository {

    private val gson = Gson()

    override fun getAllDuties(): Flow<List<Duty>> {
        // Per LOCATION_PLAN: Use reactive polling via LocationService updates
        // Combined with database changes for holistic duty list management
        return dutyDao.getAllDuties()
            .combine(locationService.locationUpdates) { entities, location ->
                // Trigger recalculation on either DB change OR location update
                entities to location
            }
            // .onEach - TRIGGER REMOVED due to billing hazard.
            // Enrichment must be an explicit, one-time action, not reactive to GPS.
            .map { (entities, locationUpdate) ->
                // Get user settings for PoNR calculation
                val userSettings = userSettingsRepository.getUserSettings().first()
                
                // CRITICAL FIX: Use the location from the live update flow!
                // Previously we called getLastKnownLocation() which might be stale.
                // If flow location is null (initial state), try getLastKnownLocation as fallback.
                val currentLocation = locationUpdate ?: try {
                    locationService.getLastKnownLocation()
                } catch (e: SecurityException) {
                    Log.w(TAG, "Location permission not granted")
                    null
                }
                
                if (currentLocation != null) {
                    Log.d(TAG, "Recalculating PoNR with location: lat=${currentLocation.latitude}, lng=${currentLocation.longitude}")
                } else {
                    Log.w(TAG, "No location available - travel times will be 0")
                }
                
                // Variable to track the minimum time to PoNR across all duties
                var minMinutesToPoNR: Int? = null

                // Filter and map to domain with PoNR calculation
                val dutiesWithPoNR = entities
                    .filter { it.dismissalReason == null && !it.isDeleted }
                    .map { entity ->
                        val duty = entity.toDomainDuty()
                        
                        // Calculate PoNR with DataQuality logic
                        val ponr = calculatePoNRUseCase(
                            duty = duty,
                            currentLocation = currentLocation,
                            defaultBufferMinutes = userSettings.defaultBufferMinutes,
                            urgencyThresholdMinutes = userSettings.urgencyTimeThreshold
                        )
                        
                        // PERSISTENCE: Update DB if significant change
                        val diff = kotlin.math.abs(ponr.commuteMinutes - (entity.lastCalculatedCommuteMinutes ?: 0))
                        val isSignificantChange = diff > 2 
                        
                        if (ponr.dataQuality != com.aliminder.app.domain.model.PoNRDataQuality.STALE &&
                            isSignificantChange) {
                            Log.d(TAG, "Persisting new commute time for '${duty.title}': ${ponr.commuteMinutes} min")
                            dutyDao.updateLastCalculatedCommute(duty.id, ponr.commuteMinutes)
                        }
                        
                        // Check if this is the "nearest" duty (smallest positive delta)
                        // Ignore duties that are already passed PoNR (negative delta) unless we want to track lateness
                        // For battery savings, we care about "Upcoming" PoNRs.
                        if (ponr.deltaMinutes > -60) { // Keep tracking even if slightly late
                             if (minMinutesToPoNR == null || ponr.deltaMinutes < minMinutesToPoNR!!) {
                                 minMinutesToPoNR = ponr.deltaMinutes
                             }
                        }

                        duty.copy(ponr = ponr)
                    }
                
                // CRITICAL FIX: The "Broken Chain"
                // Tell LocationService how urgent the situation is so it can switch to High Accuracy / Real-time
                locationService.updateNearestDutyPoNRMinutes(minMinutesToPoNR)

                dutiesWithPoNR
            }
    }
    


    companion object {
        private const val TAG = "DutyRepository"
    }
    
    override suspend fun dismissDuty(dutyId: String, reason: DismissalReason) {
        dutyDao.updateDismissalReason(dutyId, reason.name)
    }

    override suspend fun restoreDuty(dutyId: String) {
        dutyDao.updateDismissalReason(dutyId, null)
    }

    override suspend fun autoHideOverdueDuties(overdueMinutes: Int) {
        val cutoffTime = LocalDateTime.now().minusMinutes(overdueMinutes.toLong())
        Log.d("AutoHide", "Cutoff time: $cutoffTime. Hiding duties before this time.")
        val rowsAffected = dutyDao.autoHideOverdueDuties(cutoffTime)
        Log.d("AutoHide", "Rows affected: $rowsAffected")
    }

    override suspend fun restoreNewlyValidDuties(newOverdueMinutes: Int) {
        val newCutoffTime = LocalDateTime.now().minusMinutes(newOverdueMinutes.toLong())
        dutyDao.restoreNewlyValidDuties(newCutoffTime)
    }

    override suspend fun updateDutyLocation(dutyId: String, location: String) {
        dutyDao.updateLocation(dutyId, location.trim())
    }

    override suspend fun updateDutyStructuredLocation(dutyId: String, address: com.aliminder.app.domain.model.Address) {
        dutyDao.updateStructuredLocation(
            dutyId = dutyId,
            location = address.toGoogleMapsFormat(), // Keep legacy field in sync
            name = address.name,
            street = address.street,
            city = address.city,
            state = address.state,
            zip = address.zipCode
        )
    }

    override suspend fun updateDutyCustomCommute(dutyId: String, commuteMinutes: Int) {
        dutyDao.updateCustomCommute(dutyId, commuteMinutes)
    }
    
    override suspend fun acceptDuty(dutyId: String) {
        dutyDao.acceptDuty(dutyId)
    }
    
    override suspend fun denyDuty(dutyId: String) {
        dutyDao.denyDuty(dutyId)
    }

    override suspend fun deleteAllDuties() {
        dutyDao.clearAll()
    }

    override suspend fun insertAll(duties: List<Duty>) {
        val entities = duties.map { it.toDutyEntity() }
        dutyDao.insertAll(entities)
    }
}
