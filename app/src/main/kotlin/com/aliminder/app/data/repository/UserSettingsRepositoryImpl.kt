package com.aliminder.app.data.repository

import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.local.entity.UserSettingsEntity
import com.aliminder.app.domain.model.UserSettings
import com.aliminder.app.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : UserSettingsRepository {

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
}

// Mapper functions
fun UserSettingsEntity.toDomainModel(): UserSettings {
    return UserSettings(
        isFirstLaunch = isFirstLaunch,
        defaultCommuteMinutes = defaultCommuteMinutes,
        defaultPrepMinutes = defaultPrepMinutes,
        defaultBufferMinutes = defaultBufferMinutes,
        audioRespectsSilentMode = audioRespectsSilentMode,
        audioVoiceSelection = audioVoiceSelection,
        useDynamicTitleBarColor = useDynamicTitleBarColor,
        urgencyTimeThreshold = urgencyTimeThreshold
    )
}

fun UserSettings.toEntity(): UserSettingsEntity {
    return UserSettingsEntity(
        isFirstLaunch = isFirstLaunch,
        defaultCommuteMinutes = defaultCommuteMinutes,
        defaultPrepMinutes = defaultPrepMinutes,
        defaultBufferMinutes = defaultBufferMinutes,
        audioRespectsSilentMode = audioRespectsSilentMode,
        audioVoiceSelection = audioVoiceSelection,
        useDynamicTitleBarColor = useDynamicTitleBarColor,
        urgencyTimeThreshold = urgencyTimeThreshold
    )
}
