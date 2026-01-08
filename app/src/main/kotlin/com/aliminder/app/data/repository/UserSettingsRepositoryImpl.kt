package com.aliminder.app.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.local.entity.UserSettingsEntity
import com.aliminder.app.domain.model.Address
import com.aliminder.app.domain.model.UserSettings
import com.aliminder.app.domain.repository.UserSettingsRepository
import com.aliminder.app.domain.service.GeofenceService
import com.aliminder.app.domain.usecase.RestoreValidDutiesUseCase
import com.aliminder.app.domain.worker.AutoHideDutiesWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val restoreValidDutiesUseCaseProvider: Provider<RestoreValidDutiesUseCase>,
    private val geofenceService: GeofenceService,
    @ApplicationContext private val context: Context
) : UserSettingsRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun getUserSettings(): Flow<UserSettings> {
        return userSettingsDao.getUserSettings().map {
            it?.toDomainModel() ?: UserSettings()
        }
    }



    override suspend fun updateUrgencyTimeThreshold(minutes: Int) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        userSettingsDao.insert(currentSettings.copy(urgencyTimeThreshold = minutes))
    }

    override suspend fun updateAutoHideOverdueMinutes(minutes: Int) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        val oldMinutes = currentSettings.autoHideOverdueMinutes

        // If threshold increased, restore duties that are now valid
        // This must happen BEFORE saving the new setting
        // Provider used for lazy injection to avoid circular dependency
        if (minutes > oldMinutes) {
            restoreValidDutiesUseCaseProvider.get()(minutes)
        }

        // Save the new setting
        userSettingsDao.insert(currentSettings.copy(autoHideOverdueMinutes = minutes))

        // Enqueue worker to hide any duties still overdue under new setting
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<AutoHideDutiesWorker>().build()
        workManager.enqueue(oneTimeWorkRequest)
    }

    override suspend fun setHomeAddress(address: Address) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        userSettingsDao.insert(currentSettings.copy(
            homeStreet = address.street,
            homeCity = address.city,
            homeState = address.state,
            homeZip = address.zipCode
        ))
        
        // Setup geofences with updated addresses
        setupGeofences()
    }
    
    override suspend fun setWorkAddress(address: Address) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        userSettingsDao.insert(currentSettings.copy(
            workStreet = address.street,
            workCity = address.city,
            workState = address.state,
            workZip = address.zipCode
        ))
        
        // Setup geofences with updated addresses
        setupGeofences()
    }
    
    /**
     * Setup geofences around home/work addresses.
     * Called automatically when addresses are set.
     */
    private suspend fun setupGeofences() {
        val settings = userSettingsDao.getUserSettings().first() ?: return
        
        val homeAddress = settings.toDomainModel().homeAddress
        val workAddress = settings.toDomainModel().workAddress
        
        geofenceService.setupGeofences(homeAddress, workAddress)
    }
}

// Mapper functions
fun UserSettingsEntity.toDomainModel(): UserSettings {
    return UserSettings(
        isFirstLaunch = isFirstLaunch,
        // defaultPrepMinutes ignored
        defaultBufferMinutes = defaultBufferMinutes,
        audioRespectsSilentMode = audioRespectsSilentMode,
        audioVoiceSelection = audioVoiceSelection,

        urgencyTimeThreshold = urgencyTimeThreshold,
        autoHideOverdueMinutes = autoHideOverdueMinutes,
        homeAddress = if (homeStreet != null && homeCity != null && homeState != null && homeZip != null) {
            Address(homeStreet, homeCity, homeState, homeZip)
        } else null,
        workAddress = if (workStreet != null && workCity != null && workState != null && workZip != null) {
            Address(workStreet, workCity, workState, workZip)
        } else null
    )
}

fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        isFirstLaunch = isFirstLaunch,
        // defaultPrepMinutes ignored (uses default value in Entity)
        defaultBufferMinutes = defaultBufferMinutes,
        audioRespectsSilentMode = audioRespectsSilentMode,
        audioVoiceSelection = audioVoiceSelection,

        urgencyTimeThreshold = urgencyTimeThreshold,
        autoHideOverdueMinutes = autoHideOverdueMinutes,
        homeStreet = homeAddress?.street,
        homeCity = homeAddress?.city,
        homeState = homeAddress?.state,
        homeZip = homeAddress?.zipCode,
        workStreet = workAddress?.street,
        workCity = workAddress?.city,
        workState = workAddress?.state,
        workZip = workAddress?.zipCode
    )
}
