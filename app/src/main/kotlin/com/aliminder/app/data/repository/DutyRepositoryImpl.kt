package com.aliminder.app.data.repository

import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.local.entity.UserSettingsEntity
import com.aliminder.app.data.mapper.toDomainDuty
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.repository.DutyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DutyRepositoryImpl @Inject constructor(
    private val dutyDao: DutyDao,
    private val userSettingsDao: UserSettingsDao // Inject UserSettingsDao
) : DutyRepository {

    override fun getAllDuties(): Flow<List<Duty>> {
        // Ticker flow that aligns with system clock minute changes
        val ticker = flow {
            // First tick immediately
            emit(Unit)
            
            // Calculate delay until next full minute
            val now = LocalDateTime.now()
            val nextMinute = now.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES)
            val initialDelay = java.time.Duration.between(now, nextMinute).toMillis()
            
            // Wait until the start of the next minute plus 1 second
            if (initialDelay > 0) {
                delay(initialDelay + 1000L) // Add 1 second delay
                emit(Unit) // Emit 1 second after the minute mark
            }
            
            // Then tick every 60 seconds
            while (true) {
                delay(60_000L)
                emit(Unit)
            }
        }

        // Combine Duties, Ticker, AND UserSettings
        return combine(
            dutyDao.getAllDuties(), 
            ticker,
            userSettingsDao.getUserSettings()
        ) { entities, _, userSettings ->
            // Default to 60 minutes if settings not found
            val urgencyThreshold = userSettings?.urgencyTimeThreshold ?: 60
            
            entities.map { dutyEntity -> 
                dutyEntity.toDomainDuty(urgencyThresholdMinutes = urgencyThreshold) 
            }
        }
    }
}
