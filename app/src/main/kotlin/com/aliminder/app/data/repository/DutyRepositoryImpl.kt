package com.aliminder.app.data.repository

import android.util.Log
import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.mapper.toDomainDuty
import com.aliminder.app.domain.model.DismissalReason
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
    private val userSettingsDao: UserSettingsDao
) : DutyRepository {

    override fun getAllDuties(): Flow<List<Duty>> {
        // Ticker flow that aligns with the system clock minute changes
        val ticker = flow {
            while (true) {
                emit(Unit) // Emit immediately and then on every tick
                val now = LocalDateTime.now()
                val nextMinute = now.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES)
                val delayMillis = java.time.Duration.between(now, nextMinute).toMillis()
                if (delayMillis > 0) {
                    delay(delayMillis)
                }
            }
        }

        // Combine the duties flow (which updates on DB change), the user settings flow,
        // and the ticker flow. This ensures the UI refreshes when:
        // 1. The database is updated by the worker.
        // 2. The user changes a relevant setting.
        // 3. The ticker emits each minute for time-based calculations.
        return combine(
            dutyDao.getAllDuties(),
            userSettingsDao.getUserSettings(),
            ticker
        ) { duties, settings, _ ->
            val urgencyThreshold = settings?.urgencyTimeThreshold ?: 60
            duties.map { entity ->
                entity.toDomainDuty(urgencyThresholdMinutes = urgencyThreshold)
            }
        }
    }

    override suspend fun dismissDuty(dutyId: String, reason: DismissalReason) {
        dutyDao.updateDismissalReason(dutyId, reason.name)
    }

    override suspend fun restoreDuty(dutyId: String) {
        dutyDao.updateDismissalReason(dutyId, null)
    }

    override suspend fun autoHideOverdueDuties(overdueMinutes: Int) {
        val cutoffTime = LocalDateTime.now().minusMinutes(overdueMinutes.toLong())
        Log.d("AutoHide", "Cutoff time: $cutoffTime. Hiding duties before this time.")
        val rowsAffected = dutyDao.autoHideOverdueDuties(cutoffTime)
        Log.d("AutoHide", "Rows affected: $rowsAffected")
    }

    override suspend fun restoreNewlyValidDuties(newOverdueMinutes: Int) {
        val newCutoffTime = LocalDateTime.now().minusMinutes(newOverdueMinutes.toLong())
        dutyDao.restoreNewlyValidDuties(newCutoffTime)
    }
}
