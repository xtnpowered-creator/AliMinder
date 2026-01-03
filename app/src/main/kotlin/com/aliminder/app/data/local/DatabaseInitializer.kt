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
        return listOf(
            DutyEntity(
                id = "1",
                title = "Team Stand-up",
                startTime = LocalDateTime.now().plusMinutes(15),
                endTime = LocalDateTime.now().plusMinutes(45),
                provider = DutyProvider.SHADOW,
                providerDutyId = "1",
                sourceType = "SHADOW_EVENT",
                acceptanceStatus = "ACCEPTED"
            ),
            DutyEntity(
                id = "2",
                title = "1:1 with Manager",
                startTime = LocalDateTime.now().plusHours(1),
                endTime = LocalDateTime.now().plusHours(2),
                provider = DutyProvider.SHADOW,
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
                provider = DutyProvider.SHADOW,
                providerDutyId = "3",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED"
            ),
             DutyEntity(
                id = "4",
                title = "Project Kickoff Invite",
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(1).plusHours(1),
                provider = DutyProvider.SHADOW,
                providerDutyId = "4",
                sourceType = "SHADOW_EVENT",
                acceptanceStatus = "PENDING"
            ),
            // New Mock Data
             DutyEntity(
                id = "10",
                title = "Morning Gym Session",
                startTime = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "10",
                sourceType = "SHADOW_EVENT", // Personal
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 15,
                customPrepMinutes = 10,
                customBufferMinutes = 5
            ),
            DutyEntity(
                id = "11",
                title = "Code Review Meeting",
                startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(30),
                endTime = LocalDateTime.now().plusDays(1).withHour(11).withMinute(30),
                provider = DutyProvider.SHADOW,
                providerDutyId = "11",
                sourceType = "SHADOW_EVENT", // Meeting
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 0,
                customPrepMinutes = 5
            ),
            DutyEntity(
                id = "12",
                title = "Update Jira Tickets",
                startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(30),
                provider = DutyProvider.SHADOW,
                providerDutyId = "12",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED"
            ),
            DutyEntity(
                id = "13",
                title = "Dinner Prep",
                startTime = LocalDateTime.now().plusHours(6),
                endTime = LocalDateTime.now().plusHours(6).plusMinutes(30),
                provider = DutyProvider.SHADOW,
                providerDutyId = "13",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED",
                customPrepMinutes = 30
            ),
            DutyEntity(
                id = "14",
                title = "Movie Night with Friends",
                startTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(22).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "14",
                sourceType = "SHADOW_EVENT", // Social
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 25,
                customPrepMinutes = 15,
                customBufferMinutes = 10
            ),
            DutyEntity(
                id = "15",
                title = "Flight Check-in",
                startTime = LocalDateTime.now().plusDays(1).withHour(6).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(6).withMinute(15),
                provider = DutyProvider.SHADOW,
                providerDutyId = "15",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED"
            ),
            DutyEntity(
                id = "16",
                title = "Deep Work: Architecture",
                startTime = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "16",
                sourceType = "SHADOW_EVENT", // Work
                acceptanceStatus = "ACCEPTED",
                customPrepMinutes = 10
            ),
            DutyEntity(
                id = "17",
                title = "Quick Sync",
                startTime = LocalDateTime.now().plusHours(1).plusMinutes(15),
                endTime = LocalDateTime.now().plusHours(1).plusMinutes(30),
                provider = DutyProvider.SHADOW,
                providerDutyId = "17",
                sourceType = "SHADOW_EVENT", // Meeting
                acceptanceStatus = "ACCEPTED"
            ),
            DutyEntity(
                id = "18",
                title = "Doctor Follow-up (Tentative)",
                startTime = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(15).withMinute(30),
                provider = DutyProvider.SHADOW,
                providerDutyId = "18",
                sourceType = "SHADOW_EVENT", // Pending
                acceptanceStatus = "PENDING",
                customCommuteMinutes = 20,
                customBufferMinutes = 5
            ),
            DutyEntity(
                id = "19",
                title = "Quarterly Planning",
                startTime = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0),
                endTime = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "19",
                sourceType = "SHADOW_EVENT", // Meeting
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 30,
                customPrepMinutes = 15,
                customBufferMinutes = 10
            ),
            DutyEntity(
                id = "20",
                title = "Pay Utility Bills",
                startTime = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0),
                endTime = LocalDateTime.now().plusDays(2).withHour(12).withMinute(15),
                provider = DutyProvider.SHADOW,
                providerDutyId = "20",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED",
                customPrepMinutes = 5
            ),
            DutyEntity(
                id = "21",
                title = "Water Plants",
                startTime = LocalDateTime.now().plusHours(3).plusMinutes(30),
                endTime = LocalDateTime.now().plusHours(3).plusMinutes(45),
                provider = DutyProvider.SHADOW,
                providerDutyId = "21",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED",
                customPrepMinutes = 10
            ),
            DutyEntity(
                id = "22",
                title = "Coffee with Mentor",
                startTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(30),
                endTime = LocalDateTime.now().plusDays(1).withHour(9).withMinute(30),
                provider = DutyProvider.SHADOW,
                providerDutyId = "22",
                sourceType = "SHADOW_EVENT", // Social
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 15,
                customPrepMinutes = 5,
                customBufferMinutes = 5
            ),
            DutyEntity(
                id = "23",
                title = "Grocery Shopping",
                startTime = LocalDateTime.now().plusDays(2).withHour(17).withMinute(30),
                endTime = LocalDateTime.now().plusDays(2).withHour(18).withMinute(30),
                provider = DutyProvider.SHADOW,
                providerDutyId = "23",
                sourceType = "SHADOW_TASK", // Task
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 10
            ),
            DutyEntity(
                id = "24",
                title = "Team Retro",
                startTime = LocalDateTime.now().plusDays(1).withHour(16).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(17).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "24",
                sourceType = "SHADOW_EVENT", // Meeting
                acceptanceStatus = "ACCEPTED",
                customPrepMinutes = 5
            ),
            DutyEntity(
                id = "25",
                title = "Workshop: Kotlin Flows",
                startTime = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0),
                endTime = LocalDateTime.now().plusDays(2).withHour(13).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "25",
                sourceType = "SHADOW_EVENT", // Work
                acceptanceStatus = "ACCEPTED"
            ),
            DutyEntity(
                id = "26",
                title = "Journaling",
                startTime = LocalDateTime.now().plusHours(8),
                endTime = LocalDateTime.now().plusHours(8).plusMinutes(20),
                provider = DutyProvider.SHADOW,
                providerDutyId = "26",
                sourceType = "SHADOW_TASK", // Personal
                acceptanceStatus = "ACCEPTED",
                customPrepMinutes = 15
            ),
            DutyEntity(
                id = "27",
                title = "Lunch & Learn (Invite)",
                startTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "27",
                sourceType = "SHADOW_EVENT", // Pending
                acceptanceStatus = "PENDING"
            ),
            DutyEntity(
                id = "28",
                title = "Game Night",
                startTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0),
                endTime = LocalDateTime.now().plusDays(2).withHour(23).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "28",
                sourceType = "SHADOW_EVENT", // Social
                acceptanceStatus = "ACCEPTED",
                customCommuteMinutes = 20,
                customPrepMinutes = 30
            ),
            DutyEntity(
                id = "29",
                title = "Prepare Presentation Deck",
                startTime = LocalDateTime.now().plusDays(1).withHour(21).withMinute(0),
                endTime = LocalDateTime.now().plusDays(1).withHour(22).withMinute(0),
                provider = DutyProvider.SHADOW,
                providerDutyId = "29",
                sourceType = "SHADOW_TASK",
                acceptanceStatus = "ACCEPTED"
            )
        )
    }
}
