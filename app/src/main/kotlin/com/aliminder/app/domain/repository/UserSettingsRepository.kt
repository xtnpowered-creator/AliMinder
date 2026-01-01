package com.aliminder.app.domain.repository

import com.aliminder.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateDynamicTitleBarColor(enabled: Boolean)
}
