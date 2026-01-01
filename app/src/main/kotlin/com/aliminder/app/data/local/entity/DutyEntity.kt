package com.aliminder.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aliminder.app.domain.model.DutyProvider
import java.time.LocalDateTime

@Entity(tableName = "duties")
data class DutyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String? = null,
    val provider: DutyProvider,
    val providerDutyId: String, // The ID from the original source (e.g., M365 event ID)
    val sourceType: String, // e.g., M365_CALENDAR_EVENT, SHADOW_TASK
    val acceptanceStatus: String, // e.g., ACCEPTED, TENTATIVE, PENDING
    val customCommuteMinutes: Int? = null,
    val customPrepMinutes: Int? = null,
    val customBufferMinutes: Int? = null,
    val isAllDay: Boolean = false,
    val isDeleted: Boolean = false // For soft deletes
)
