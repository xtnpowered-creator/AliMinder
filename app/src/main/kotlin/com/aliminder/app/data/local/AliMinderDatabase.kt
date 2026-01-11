package com.aliminder.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.local.entity.DutyEntity
import com.aliminder.app.data.local.entity.UserSettingsEntity

@Database(
    entities = [DutyEntity::class, UserSettingsEntity::class],
    version = 9, // Incremented for Rich Duty Details (Organizer, Checklist, etc.)
    exportSchema = false
)
@TypeConverters(AliMinderTypeConverters::class)
abstract class AliMinderDatabase : RoomDatabase() {
    abstract fun dutyDao(): DutyDao
    abstract fun userSettingsDao(): UserSettingsDao
}
