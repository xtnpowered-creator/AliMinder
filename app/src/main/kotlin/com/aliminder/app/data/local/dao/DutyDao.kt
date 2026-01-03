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
}
