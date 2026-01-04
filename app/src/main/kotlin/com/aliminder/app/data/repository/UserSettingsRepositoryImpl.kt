package com.aliminder.app.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.local.entity.UserSettingsEntity
import com.aliminder.app.domain.model.UserSettings
import com.aliminder.app.domain.repository.DutyRepository
import com.aliminder.app.domain.repository.UserSettingsRepository
import com.aliminder.app.domain.worker.AutoHideDutiesWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val dutyRepository: DutyRepository, // Injected to handle duty restoration
    @ApplicationContext private val context: Context
) : UserSettingsRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun getUserSettings(): Flow<UserSettings> {
        return userSettingsDao.getUserSettings().map {
            it?.toDomainModel() ?: UserSettings()
        }
    }

    override suspend fun updateDynamicTitleBarColor(enabled: Boolean) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        userSettingsDao.insert(currentSettings.copy(useDynamicTitleBarColor = enabled))
    }

    override suspend fun updateUrgencyTimeThreshold(minutes: Int) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        userSettingsDao.insert(currentSettings.copy(urgencyTimeThreshold = minutes))
    }

    override suspend fun updateAutoHideOverdueMinutes(minutes: Int) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        val oldMinutes = currentSettings.autoHideOverdueMinutes

        // If the new timeframe is longer, restore duties that are now valid.
        // This must be done *before* saving the new setting.
        if (minutes > oldMinutes) {
            dutyRepository.restoreNewlyValidDuties(minutes)
        }

        // Save the new setting.
        userSettingsDao.insert(currentSettings.copy(autoHideOverdueMinutes = minutes))

        // Finally, enqueue the worker to hide any duties that are still overdue under the new setting.
        // This runs after the restoration logic has completed.
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<AutoHideDutiesWorker>().build()
        workManager.enqueue(oneTimeWorkRequest)
    }

    override suspend fun updateHomeAddress(address: String) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        userSettingsDao.insert(currentSettings.copy(homeAddress = address.trim()))
    }

    override suspend fun updateWorkAddress(address: String) {
        val currentSettings = userSettingsDao.getUserSettings().first() ?: UserSettingsEntity()
        userSettingsDao.insert(currentSettings.copy(workAddress = address.trim()))
    }
}

// Mapper functions
fun UserSettingsEntity.toDomainModel(): UserSettings {
    return UserSettings(
        isFirstLaunch = isFirstLaunch,
        defaultPrepMinutes = defaultPrepMinutes,
        defaultBufferMinutes = defaultBufferMinutes,
        audioRespectsSilentMode = audioRespectsSilentMode,
        audioVoiceSelection = audioVoiceSelection,
        useDynamicTitleBarColor = useDynamicTitleBarColor,
        urgencyTimeThreshold = urgencyTimeThreshold,
        autoHideOverdueMinutes = autoHideOverdueMinutes,
        homeAddress = homeAddress,
        workAddress = workAddress
    )
}

fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        isFirstLaunch = isFirstLaunch,
        defaultPrepMinutes = defaultPrepMinutes,
        defaultBufferMinutes = defaultBufferMinutes,
        audioRespectsSilentMode = audioRespectsSilentMode,
        audioVoiceSelection = audioVoiceSelection,
        useDynamicTitleBarColor = useDynamicTitleBarColor,
        urgencyTimeThreshold = urgencyTimeThreshold,
        autoHideOverdueMinutes = autoHideOverdueMinutes,
        homeAddress = homeAddress,
        workAddress = workAddress
    )
}
