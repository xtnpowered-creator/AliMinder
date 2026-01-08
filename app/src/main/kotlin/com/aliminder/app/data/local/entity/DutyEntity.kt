package com.aliminder.app.data.local.entity

import androidx.room.ColumnInfo
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
    
    // Structured Address Fields
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    @ColumnInfo(name = "location_street") val locationStreet: String? = null,
    @ColumnInfo(name = "location_city") val locationCity: String? = null,
    @ColumnInfo(name = "location_state") val locationState: String? = null,
    @ColumnInfo(name = "location_zip") val locationZip: String? = null,
    
    val provider: DutyProvider,
    val providerDutyId: String, // The ID from the original source (e.g., M365 event ID)
    val sourceType: String, // e.g., M365_CALENDAR_EVENT, SHADOW_TASK
    val acceptanceStatus: String, // e.g., ACCEPTED, TENTATIVE, PENDING
    val customCommuteMinutes: Int? = null,
    val customPrepMinutes: Int? = null,
    val customBufferMinutes: Int? = null,
    val isAllDay: Boolean = false,
    val isDeleted: Boolean = false, // For soft deletes (Legacy, maybe can be removed or kept for backward comp)
    @ColumnInfo(name = "dismissal_reason")
    val dismissalReason: String? = null, // COMPLETED, CANCELLED, USER_HIDDEN, AUTO_HIDDEN
    @ColumnInfo(name = "last_calculated_commute_minutes")
    val lastCalculatedCommuteMinutes: Int? = null,
    @ColumnInfo(name = "virtual_meeting_link")
    val virtualMeetingLink: String? = null
)
