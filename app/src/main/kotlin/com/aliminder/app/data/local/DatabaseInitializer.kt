package com.aliminder.app.data.local

import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.local.entity.DutyEntity
import com.aliminder.app.data.local.entity.UserSettingsEntity
import com.aliminder.app.domain.model.DutyProvider
import java.time.LocalDateTime
import javax.inject.Inject

class DatabaseInitializer @Inject constructor(
    private val dutyDao: DutyDao,
    private val userSettingsDao: UserSettingsDao
) {

    suspend fun initialize()
    {
        if (userSettingsDao.getCount() == 0) {
            // First launch, insert default settings and mock data
            userSettingsDao.insert(UserSettingsEntity(isFirstLaunch = false))
            dutyDao.insertAll(getInitialDuties())
        }
    }

    private fun getInitialDuties(): List<DutyEntity> {
        return RealisticMockDataGenerator.generate()
    }
}
