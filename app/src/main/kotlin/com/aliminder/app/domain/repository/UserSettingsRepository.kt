package com.aliminder.app.domain.repository

import com.aliminder.app.domain.model.Address
import com.aliminder.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings>

    suspend fun updateUrgencyTimeThreshold(minutes: Int)
    suspend fun updateAutoHideOverdueMinutes(minutes: Int)
    suspend fun setHomeAddress(address: Address)
    suspend fun setWorkAddress(address: Address)
}
