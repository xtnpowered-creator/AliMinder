package com.aliminder.app.di

import android.content.Context
import androidx.room.Room
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
            context,
            AliMinderDatabase::class.java,
            "aliminder_database"
        )
        .fallbackToDestructiveMigration() // Added this to fix the crash
        .build()
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
