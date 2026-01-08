package com.aliminder.app.domain.repository

import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.Address
import kotlinx.coroutines.flow.Flow

interface DutyRepository {
    fun getAllDuties(): Flow<List<Duty>>
    suspend fun dismissDuty(dutyId: String, reason: DismissalReason)
    suspend fun restoreDuty(dutyId: String)
    suspend fun autoHideOverdueDuties(overdueMinutes: Int)
    suspend fun restoreNewlyValidDuties(newOverdueMinutes: Int)


    suspend fun updateDutyLocation(dutyId: String, location: String)
    suspend fun updateDutyStructuredLocation(dutyId: String, address: Address)
    suspend fun updateDutyCustomCommute(dutyId: String, commuteMinutes: Int)
    suspend fun acceptDuty(dutyId: String)
    suspend fun denyDuty(dutyId: String)
    suspend fun deleteAllDuties()
    suspend fun insertAll(duties: List<Duty>)
}
