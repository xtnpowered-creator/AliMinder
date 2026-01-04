package com.aliminder.app.domain.repository

import com.aliminder.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateDynamicTitleBarColor(enabled: Boolean)
    suspend fun updateUrgencyTimeThreshold(minutes: Int)
    suspend fun updateAutoHideOverdueMinutes(minutes: Int)
    suspend fun updateHomeAddress(address: String)
    suspend fun updateWorkAddress(address: String)
}
