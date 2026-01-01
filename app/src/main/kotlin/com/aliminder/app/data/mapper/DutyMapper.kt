package com.aliminder.app.data.mapper

import com.aliminder.app.data.local.entity.DutyEntity
import com.aliminder.app.domain.model.Duty
import com.aliminder.app.domain.model.PoNRCalculation
import com.aliminder.app.domain.model.PersonaStage
import java.time.Duration
import java.time.LocalDateTime

fun DutyEntity.toDomainDuty(urgencyThresholdMinutes: Int = 60): Duty {
    // Basic PoNR calculation
    val commute = customCommuteMinutes ?: 20
    val prep = customPrepMinutes ?: 15
    val buffer = customBufferMinutes ?: 10
    val ponrTime = startTime.minusMinutes(commute.toLong() + prep.toLong() + buffer.toLong())
    val now = LocalDateTime.now()
    val delta = Duration.between(now, ponrTime).toMinutes().toInt()

    // Determine Persona Stage based on delta and start time
    val personaStage = when {
        now.isAfter(startTime) -> PersonaStage.LATE
        delta > urgencyThresholdMinutes -> PersonaStage.OPTIMISTIC
        delta in 0..urgencyThresholdMinutes -> PersonaStage.WEARY
        else -> PersonaStage.URGENT
    }

    // Determine Category based on acceptance status first
    val category = if (acceptanceStatus == "PENDING") {
        "Pending"
    } else {
        sourceType // Fallback to sourceType (e.g., SHADOW_TASK, SHADOW_EVENT)
    }

    return Duty(
        id = id,
        title = title,
        description = description,
        startTime = startTime,
        endTime = endTime,
        location = location,
        provider = provider,
        customCommuteMinutes = customCommuteMinutes,
        customPrepMinutes = customPrepMinutes,
        customBufferMinutes = customBufferMinutes,
        category = category, // Use the correctly determined category
        ponr = PoNRCalculation(
            eventId = id,
            eventTime = startTime,
            commuteMinutes = commute,
            prepMinutes = prep,
            bufferMinutes = buffer,
            ponrTime = ponrTime,
            deltaMinutes = delta,
            personaStage = personaStage
        ),
        delta = delta,
        isAllDay = isAllDay,
        isDismissed = isDeleted
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
        provider = provider,
        providerDutyId = id, // Assuming the domain ID is the provider ID for now
        sourceType = category ?: "SHADOW_EVENT",
        acceptanceStatus = if (category == "Pending") "PENDING" else "ACCEPTED",
        customCommuteMinutes = customCommuteMinutes,
        customPrepMinutes = customPrepMinutes,
        customBufferMinutes = customBufferMinutes,
        isAllDay = isAllDay,
        isDeleted = isDismissed
    )
}
