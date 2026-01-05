package com.aliminder.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aliminder.app.data.local.AliMinderDatabase
import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.repository.DutyRepositoryImpl
import com.aliminder.app.data.repository.UserSettingsRepositoryImpl
import com.aliminder.app.domain.repository.DutyRepository
import com.aliminder.app.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAliMinderDatabase(
        @ApplicationContext context: Context
    ): AliMinderDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AliMinderDatabase::class.java,
            "aliminder_database"
        )
            .fallbackToDestructiveMigration() // For development - will reset DB on schema change
            .addMigrations(MIGRATION_3_4)
            .build()
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add structured address columns for home address
            db.execSQL("ALTER TABLE user_settings ADD COLUMN home_street TEXT")
            db.execSQL("ALTER TABLE user_settings ADD COLUMN home_city TEXT")
            db.execSQL("ALTER TABLE user_settings ADD COLUMN home_state TEXT")
            db.execSQL("ALTER TABLE user_settings ADD COLUMN home_zip TEXT")
            
            // Add structured address columns for work address
            db.execSQL("ALTER TABLE user_settings ADD COLUMN work_street TEXT")
            db.execSQL("ALTER TABLE user_settings ADD COLUMN work_city TEXT")
            db.execSQL("ALTER TABLE user_settings ADD COLUMN work_state TEXT")
            db.execSQL("ALTER TABLE user_settings ADD COLUMN work_zip TEXT")
            
            // SQLite doesn't support column rename or drop directly, so we need to:
            // 1. Create new table with correct schema
            // 2. Copy data
            // 3. Drop old table
            // 4. Rename new table
            
            db.execSQL("""
                CREATE TABLE user_settings_new (
                    id INTEGER PRIMARY KEY NOT NULL,
                    isFirstLaunch INTEGER NOT NULL,
                    defaultPrepMinutes INTEGER NOT NULL,
                    defaultBufferMinutes INTEGER NOT NULL,
                    audioRespectsSilentMode INTEGER NOT NULL,
                    audioVoiceSelection TEXT,
                    useDynamicTitleBarColor INTEGER NOT NULL,
                    urgencyTimeThreshold INTEGER NOT NULL,
                    auto_hide_overdue_minutes INTEGER NOT NULL,
                    home_street TEXT,
                    home_city TEXT,
                    home_state TEXT,
                    home_zip TEXT,
                    work_street TEXT,
                    work_city TEXT,
                    work_state TEXT,
                    work_zip TEXT
                )
            """.trimIndent())
            
            // Copy data from old table (excluding homeAddress and workAddress)
            db.execSQL("""
                INSERT INTO user_settings_new (
                    id, isFirstLaunch, defaultPrepMinutes, defaultBufferMinutes,
                    audioRespectsSilentMode, audioVoiceSelection, useDynamicTitleBarColor,
                    urgencyTimeThreshold, auto_hide_overdue_minutes,
                    home_street, home_city, home_state, home_zip,
                    work_street, work_city, work_state, work_zip
                )
                SELECT 
                    id, isFirstLaunch, defaultPrepMinutes, defaultBufferMinutes,
                    audioRespectsSilentMode, audioVoiceSelection, useDynamicTitleBarColor,
                    urgencyTimeThreshold, autoHideOverdueMinutes,
                    home_street, home_city, home_state, home_zip,
                    work_street, work_city, work_state, work_zip
                FROM user_settings
            """.trimIndent())
            
            // Drop old table
            db.execSQL("DROP TABLE user_settings")
            
            // Rename new table to original name
            db.execSQL("ALTER TABLE user_settings_new RENAME TO user_settings")
        }
    }

    @Provides
    fun provideDutyDao(database: AliMinderDatabase): DutyDao = database.dutyDao()

    @Provides
    fun provideUserSettingsDao(database: AliMinderDatabase): UserSettingsDao = database.userSettingsDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDutyRepository(dutyRepositoryImpl: DutyRepositoryImpl): DutyRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(userSettingsRepositoryImpl: UserSettingsRepositoryImpl): UserSettingsRepository
}
