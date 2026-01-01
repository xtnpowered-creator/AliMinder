package com.aliminder.app.data.local

import android.content.Context
import com.aliminder.app.data.local.dao.DutyDao
import com.aliminder.app.data.local.dao.UserSettingsDao
import com.aliminder.app.data.local.entity.DutyEntity
import com.aliminder.app.data.local.entity.UserSettingsEntity
import com.aliminder.app.domain.model.EventProvider
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
        return listOf(
            DutyEntity(
                id = "1",
                title = "Team Stand-up",
                startTime = LocalDateTime.now().plusMinutes(15),
                endTime = LocalDateTime.now().plusMinutes(45),
                provider = EventProvider.SHADOW,
                providerDutyId = "1",
                sourceType = "SHADOW_EVENT",
                acceptanceStatus = "ACCEPTED"
            ),
            DutyEntity(
                id = "2",
                title = "1:1 with Manager",
                startTime = LocalDateTime.now().plusHours(1),
                endTime = LocalDateTime.now().plusHours(2),
                provider = EventProvider.SHADOW,
                providerDutyId = "2",
                sourceType = "SHADOW_EVENT",
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 20
            ),
            DutyEntity(
                id = "3",
                title = "Submit Weekly Report",
                startTime = LocalDateTime.now().plusHours(4),
                endTime = LocalDateTime.now().plusHours(5),
                provider = EventProvider.SHADOW,
                providerDutyId = "3",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED"
            ),
             DutyEntity(
                id = "4",
                title = "Project Kickoff Invite",
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(1).plusHours(1),
                provider = EventProvider.SHADOW,
                providerDutyId = "4",
                sourceType = "SHADOW_EVENT",
                acceptanceStatus = "PENDING" // This is a pending item
            )
        )
    }
}
