package com.aliminder.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aliminder.app.data.local.entity.DutyEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface DutyDao {

    // Return all duties, including soft-deleted ones (we filter in repo/mapper now based on use case)
    @Query("SELECT * FROM duties ORDER BY startTime ASC")
    fun getAllDuties(): Flow<List<DutyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(duties: List<DutyEntity>)

    @Query("DELETE FROM duties")
    suspend fun clearAll()

    @Query("UPDATE duties SET dismissal_reason = :reason, isDeleted = CASE WHEN :reason IS NULL THEN 0 ELSE 1 END WHERE id = :dutyId")
    suspend fun updateDismissalReason(dutyId: String, reason: String?)
    
    // Helper to soft delete (legacy support if needed, but updateDismissalReason handles it)
    @Query("UPDATE duties SET isDeleted = 1 WHERE id = :dutyId")
    suspend fun softDelete(dutyId: String)

    // New method for auto-hide - Changed to startTime and LocalDateTime
    @Query("UPDATE duties SET dismissal_reason = 'AUTO_HIDDEN' WHERE dismissal_reason IS NULL AND startTime < :cutoffTime")
    suspend fun autoHideOverdueDuties(cutoffTime: LocalDateTime): Int

    // Accepts LocalDateTime for comparison
    @Query("UPDATE duties SET dismissal_reason = NULL WHERE dismissal_reason = 'AUTO_HIDDEN' AND startTime >= :newCutoffTime")
    suspend fun restoreNewlyValidDuties(newCutoffTime: LocalDateTime)

    @Query("UPDATE duties SET location = :location WHERE id = :dutyId")
    suspend fun updateLocation(dutyId: String, location: String)

    @Query("UPDATE duties SET location = :location, location_name = :name, location_street = :street, location_city = :city, location_state = :state, location_zip = :zip WHERE id = :dutyId")
    suspend fun updateStructuredLocation(dutyId: String, location: String, name: String?, street: String?, city: String?, state: String?, zip: String?)

    @Query("UPDATE duties SET customCommuteMinutes = :commuteMinutes WHERE id = :dutyId")
    suspend fun updateCustomCommute(dutyId: String, commuteMinutes: Int)
    
    @Query("UPDATE duties SET acceptanceStatus = 'ACCEPTED' WHERE id = :dutyId")
    suspend fun acceptDuty(dutyId: String)
    
    @Query("UPDATE duties SET dismissal_reason = 'USER_HIDDEN' WHERE id = :dutyId")
    suspend fun denyDuty(dutyId: String)

    @Query("UPDATE duties SET last_calculated_commute_minutes = :minutes WHERE id = :dutyId")
    suspend fun updateLastCalculatedCommute(dutyId: String, minutes: Int)
}
