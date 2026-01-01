package com.aliminder.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aliminder.app.data.local.entity.DutyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DutyDao {

    @Query("SELECT * FROM duties WHERE isDeleted = 0 ORDER BY startTime ASC")
    fun getAllDuties(): Flow<List<DutyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(duties: List<DutyEntity>)

    @Query("DELETE FROM duties")
    suspend fun clearAll()
}
