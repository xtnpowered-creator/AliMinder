package com.aliminder.app.data.mapper

import com.aliminder.app.data.local.entity.DutyEntity
import com.aliminder.app.domain.model.DismissalReason
import com.aliminder.app.domain.model.Duty

/**
 * Convert DutyEntity to domain Duty (without PoNR calculation).
 * PoNR is calculated separately by CalculatePoNRUseCase in the repository.
 */
fun DutyEntity.toDomainDuty(): Duty {
    // Determine Category based on acceptance status first
    // Determine Category based on acceptance status first
    val category = when {
        acceptanceStatus == "PENDING" -> "Pending"
        // Prioritize Task keywords
        sourceType.contains("TASK", ignoreCase = true) || 
        sourceType.contains("TODO", ignoreCase = true) ||
        sourceType.contains("PLAN", ignoreCase = true) || // MS-PLAN, MS-PLANNER
        sourceType.contains("GW-TASK", ignoreCase = true) -> "Task"
        
        // Then check for Events
        sourceType.contains("CAL", ignoreCase = true) || 
        sourceType.contains("EVENT", ignoreCase = true) -> "Event"
        
        // Fallback team approvals to Task
        sourceType.contains("TEAM", ignoreCase = true) || sourceType.contains("APPROVAL", ignoreCase = true) -> "Task" 
        else -> "Event" // Default fallback
    }

    return Duty(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        structuredLocation = if (locationStreet != null && locationCity != null && locationState != null && locationZip != null) {
            com.aliminder.app.domain.model.Address(
                name = locationName,
                street = locationStreet,
                city = locationCity,
                state = locationState,
                zipCode = locationZip
            )
        } else {
            null
        },
        provider = provider,
        customCommuteMinutes = customCommuteMinutes,
        // customPrepMinutes ignored
        customBufferMinutes = customBufferMinutes,
        category = category,
        sourceTag = sourceType, // Pass the raw source type as the tag (e.g., "MS-CAL")
        ponr = null, // Will be calculated by Use Case in repository
        delta = Int.MAX_VALUE, // Will be set by repository
        isAllDay = isAllDay,
        dismissalReason = dismissalReason?.let { runCatching { DismissalReason.valueOf(it) }.getOrNull() } 
            ?: if (isDeleted) DismissalReason.USER_HIDDEN else null,
        lastCalculatedCommuteMinutes = lastCalculatedCommuteMinutes,
        virtualMeetingLink = virtualMeetingLink,
        priority = try { com.aliminder.app.domain.model.DutyPriority.valueOf(priority) } catch (e: Exception) { com.aliminder.app.domain.model.DutyPriority.NORMAL },
        attendees = attendees?.let { json -> 
            try { 
                val type = object : com.google.gson.reflect.TypeToken<List<com.aliminder.app.domain.model.Attendee>>() {}.type
                com.google.gson.Gson().fromJson(json, type) 
            } catch(e: Exception) { emptyList() }
        } ?: emptyList(),
        organizer = organizer?.let { json ->
            try { com.google.gson.Gson().fromJson(json, com.aliminder.app.domain.model.Attendee::class.java) } catch(e: Exception) { null }
        },
        checklist = checklist?.let { json ->
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<com.aliminder.app.domain.model.ChecklistItem>>() {}.type
                com.google.gson.Gson().fromJson(json, type)
            } catch(e: Exception) { emptyList() }
        } ?: emptyList()
    )
}

fun Duty.toDutyEntity(): DutyEntity {
    return DutyEntity(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        locationName = structuredLocation?.name,
        locationStreet = structuredLocation?.street,
        locationCity = structuredLocation?.city,
        locationState = structuredLocation?.state,
        locationZip = structuredLocation?.zipCode,
        provider = provider,
        providerDutyId = id, // Assuming the domain ID is the provider ID for now
        sourceType = sourceTag ?: category ?: "SHADOW_EVENT", // Prefer sourceTag
        acceptanceStatus = if (category == "Pending") "PENDING" else "ACCEPTED",
        customCommuteMinutes = customCommuteMinutes,
        // customPrepMinutes ignored
        customBufferMinutes = customBufferMinutes,
        isAllDay = isAllDay,
        isDeleted = isDismissed,
        dismissalReason = dismissalReason?.name,
        lastCalculatedCommuteMinutes = lastCalculatedCommuteMinutes,
        virtualMeetingLink = virtualMeetingLink,
        priority = priority.name,
        attendees = if (attendees.isNotEmpty()) com.google.gson.Gson().toJson(attendees) else null,
        organizer = if (organizer != null) com.google.gson.Gson().toJson(organizer) else null,
        checklist = if (checklist.isNotEmpty()) com.google.gson.Gson().toJson(checklist) else null
    )
}
