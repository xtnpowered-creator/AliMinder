package com.aliminder.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aliminder.app.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: UserSettingsEntity)
    
    @Query("SELECT COUNT(*) FROM user_settings")
    suspend fun getCount(): Int
}
