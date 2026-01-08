package com.aliminder.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1, // Singleton
    val isFirstLaunch: Boolean = true,
    val defaultPrepMinutes: Int = 15,
    val defaultBufferMinutes: Int = 10,
    val audioRespectsSilentMode: Boolean = false,
    val audioVoiceSelection: String? = null,
    val useDynamicTitleBarColor: Boolean = true,
    val urgencyTimeThreshold: Int = 60, // 30, 60, or 90 minutes
    @ColumnInfo(name = "auto_hide_overdue_minutes") val autoHideOverdueMinutes: Int = 120, // Default 2 hours (120 minutes). Options: 30, 60, 120, 180
    
    // Home Address (structured)
    @ColumnInfo(name = "home_street") val homeStreet: String? = null,
    @ColumnInfo(name = "home_city") val homeCity: String? = null,
    @ColumnInfo(name = "home_state") val homeState: String? = null,
    @ColumnInfo(name = "home_zip") val homeZip: String? = null,
    
    // Work Address (structured)
    @ColumnInfo(name = "work_street") val workStreet: String? = null,
    @ColumnInfo(name = "work_city") val workCity: String? = null,
    @ColumnInfo(name = "work_state") val workState: String? = null,
    @ColumnInfo(name = "work_zip") val workZip: String? = null
)
