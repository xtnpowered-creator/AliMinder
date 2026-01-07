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

@Singleton
class DutyRepositoryImpl @Inject constructor(
    private val dutyDao: DutyDao,
    private val userSettingsDao: UserSettingsDao,
    private val userSettingsRepository: com.aliminder.app.domain.repository.UserSettingsRepository,
    private val locationService: com.aliminder.app.domain.service.LocationService,
    private val calculatePoNRUseCase: CalculatePoNRUseCase
) : DutyRepository {


    override fun getAllDuties(): Flow<List<Duty>> {
        // Per LOCATION_PLAN: Use reactive polling via LocationService updates
        // Combined with database changes for holistic duty list management
        return dutyDao.getAllDuties()
            .combine(locationService.locationUpdates) { entities, location ->
                // Trigger recalculation on either DB change OR location update
                entities to location
            }
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
                
                // Filter and map to domain with PoNR calculation
                val dutiesWithPoNR = entities
                    .filter { it.dismissalReason == null && !it.isDeleted }
                    .map { entity ->
                        val duty = entity.toDomainDuty()
                        
                            // Calculate PoNR with current location
                            // Calculate PoNR with DataQuality logic
                        val ponr = calculatePoNRUseCase(
                            duty = duty,
                            currentLocation = currentLocation,
                            // defaultPrepMinutes removed
                            defaultBufferMinutes = userSettings.defaultBufferMinutes,
                            urgencyThresholdMinutes = userSettings.urgencyTimeThreshold
                        )
                        
                        // PERSISTENCE: If we calculated a fresh commute time (GOOD/COARSE),
                        // and it differs SIGNIFICANTLY from what we have stored, update the database.
                        // We use a threshold (JITTER) to prevent infinite loops where slight GPS drift
                        // triggers a DB update -> which triggers a flow re-emission -> which triggers a new calc -> loop.
                        val diff = kotlin.math.abs(ponr.commuteMinutes - (entity.lastCalculatedCommuteMinutes ?: 0))
                        val isSignificantChange = diff > 2 // 2 minutes threshold
                        
                        if (ponr.dataQuality != com.aliminder.app.domain.model.PoNRDataQuality.STALE &&
                            isSignificantChange) {
                            
                            // Side-effect: Update DB. 
                            Log.d(TAG, "Persisting new commute time for '${duty.title}': ${ponr.commuteMinutes} min (was ${entity.lastCalculatedCommuteMinutes})")
                            dutyDao.updateLastCalculatedCommute(duty.id, ponr.commuteMinutes)
                        }
                        
                        duty.copy(ponr = ponr)
                    }
                
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
