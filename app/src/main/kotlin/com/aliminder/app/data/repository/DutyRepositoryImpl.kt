package com.aliminder.app.data.repository

import android.util.Log
import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.mapper.toDomainDuty
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
            .map { (entities, _) ->
                // Get user settings for PoNR calculation
                val userSettings = userSettingsRepository.getUserSettings().first()
                
                // Get current location (may have just been updated)
                val currentLocation = try {
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
                        val ponr = calculatePoNRUseCase(
                            duty = duty,
                            currentLocation = currentLocation,
                            userHomeAddress = userSettings.homeAddress,
                            userWorkAddress = userSettings.workAddress,
                            defaultPrepMinutes = userSettings.defaultPrepMinutes,
                            defaultBufferMinutes = userSettings.defaultBufferMinutes,
                            urgencyThresholdMinutes = userSettings.urgencyTimeThreshold
                        )
                        
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
}
