package com.aliminder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1, // Singleton
    val isFirstLaunch: Boolean = true,
    val defaultCommuteMinutes: Int = 20,
    val defaultPrepMinutes: Int = 15,
    val defaultBufferMinutes: Int = 10,
    val audioRespectsSilentMode: Boolean = false,
    val audioVoiceSelection: String? = null,
    val useDynamicTitleBarColor: Boolean = true,
    val urgencyTimeThreshold: Int = 60 // New setting: 30, 60, or 90 minutes
)
