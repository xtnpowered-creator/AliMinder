package com.aliminder.app.domain.repository

import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty
import kotlinx.coroutines.flow.Flow

interface DutyRepository {
    fun getAllDuties(): Flow<List<Duty>>
    suspend fun dismissDuty(dutyId: String, reason: DismissalReason)
    suspend fun restoreDuty(dutyId: String)
    suspend fun autoHideOverdueDuties(overdueMinutes: Int)
    suspend fun restoreNewlyValidDuties(newOverdueMinutes: Int)
}
