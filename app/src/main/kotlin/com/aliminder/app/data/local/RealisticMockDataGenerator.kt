package com.aliminder.app.data.local

import com.aliminder.app.data.local.entity.DutyEntity
import com.aliminder.app.domain.model.DutyProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Generates realistic mock data for a specific persona:
 * "Purchasing Director at Mattamy Homes (Farmers Branch, TX) + Mother in McKinney, TX".
 *
 * Simulates data from:
 * - MS365 (Work: Outlook, Teams, Planner, ToDo)
 * - Google Workspace (Personal: Calendar, Tasks)
 */
object RealisticMockDataGenerator {

    // Locations
    // Locations (Real Addresses for Geocoding)
    private const val LOC_HOME_MCKINNEY = "3209 Vally Forge, McKinney, TX 75070" // User Home Base
    private const val LOC_WORK_FARMERS_BRANCH = "1801 Wittington Place, Farmers Branch, TX 75234" // Mattamy Homes Office
    private const val LOC_SITE_RIVERTOWN = "500 River Rd, Melissa, TX 75454" // Valid street in Melissa
    private const val LOC_LUNCH_CAFE = "3755 W University Dr, McKinney, TX 75071" // Panera Bread
    private const val LOC_YOGA_STUDIO = "3050 S Central Expy, McKinney, TX 75070" // Near CorePower Yoga
    private const val LOC_DINNER_BBQ = "1301 N Tennessee St, McKinney, TX 75069" // Hutchins BBQ
    private const val LOC_LUNCH_WORK = "3601 Dallas Parkway, Plano, TX 75093" // Whiskey Cake
    private const val LOC_GRADUATION = "800 W Campbell Rd, Richardson, TX 75080" // UT Dallas

    fun generate(referenceDate: LocalDate = LocalDate.now()): List<DutyEntity> {
        val duties = mutableListOf<DutyEntity>()

        // 1. PAST DUTIES
        duties.add(createEvent(
            title = "Subcontractor Sync: Plumbing",
            date = referenceDate.minusDays(2),
            time = LocalTime.of(9, 30),
            durationMin = 60,
            location = LOC_SITE_RIVERTOWN,
            sourceType = "MS-CAL",
            provider = DutyProvider.MICROSOFT_365
        ))

        // 2. TODAY
        // Morning Gym
        duties.add(createEvent(
            title = "Morning Yoga",
            date = referenceDate,
            time = LocalTime.of(6, 0),
            durationMin = 60,
            location = LOC_YOGA_STUDIO,
            sourceType = "GW-CAL",
            provider = DutyProvider.GOOGLE_WORKSPACE
        ))

        // Work: Department Lunch (Event)
        duties.add(createEvent(
            title = "Purchasing Dept Monthly Lunch",
            date = referenceDate,
            time = LocalTime.of(12, 0),
            durationMin = 90,
            location = LOC_LUNCH_WORK,
            sourceType = "MS-CAL",
            provider = DutyProvider.MICROSOFT_365
        ))

        // Work: Site Visit (Event)
        duties.add(createEvent(
            title = "Site Walkthrough: Phase 3 Framing",
            date = referenceDate,
            time = LocalTime.of(14, 0),
            durationMin = 90,
            location = LOC_SITE_RIVERTOWN,
            sourceType = "MS-CAL",
            provider = DutyProvider.MICROSOFT_365
        ))

        // Work Task: Estimating
        duties.add(createTask(
            title = "Review Q2 Lumber Estimates",
            dueDate = referenceDate,
            dueTime = LocalTime.of(16, 0),
            sourceType = "MS-TODO",
            provider = DutyProvider.MICROSOFT_365
        ))
        
        // Work Task: Vendor Proposals
        duties.add(createTask(
            title = "Evaluate Drywall Subcontractor Bids",
            dueDate = referenceDate,
            dueTime = LocalTime.of(17, 30),
            sourceType = "MS-TODO",
            provider = DutyProvider.MICROSOFT_365
        ))
        
        // Personal: Dinner
        duties.add(createEvent(
            title = "Dinner with the Millers",
            date = referenceDate,
            time = LocalTime.of(18, 30),
            durationMin = 120,
            location = LOC_DINNER_BBQ,
            sourceType = "GW-CAL",
            provider = DutyProvider.GOOGLE_WORKSPACE
        ))

        // 3. UPCOMING
        
        // Work: Directors Sync (Teams Meeting)
        duties.add(createEvent(
            title = "Directors' Strategy Sync (Teams)",
            date = referenceDate.plusDays(1),
            time = LocalTime.of(9, 30),
            durationMin = 60,
            location = null, // No physical location!
            virtualLink = "Microsoft Teams Meeting", // Explicit remote link
            sourceType = "MS-CAL",
            provider = DutyProvider.MICROSOFT_365
        ))

        // Work: Site Selection
        duties.add(createEvent(
            title = "New Neighborhood Site Selection",
            date = referenceDate.plusDays(1),
            time = LocalTime.of(13, 0),
            durationMin = 120,
            location = "Celina, TX", 
            sourceType = "MS-CAL",
            provider = DutyProvider.MICROSOFT_365
        ))
        
        // Work Task: PO Issuance
        duties.add(createTask(
            title = "Issue Final POs for Phase 3",
            dueDate = referenceDate.plusDays(1),
            dueTime = LocalTime.of(15, 0),
            sourceType = "MS-TODO",
            provider = DutyProvider.MICROSOFT_365
        ))
        
        // Work Task: Planner Item (MS-PLAN)
        duties.add(createTask(
            title = "Update Q3 Project Timeline",
            dueDate = referenceDate.plusDays(2),
            dueTime = LocalTime.of(14, 0),
            sourceType = "MS-PLAN",
            provider = DutyProvider.MICROSOFT_365
        ))
        
        // Pending: Change Order
        duties.add(createEvent(
            title = "Approve Change Order #402 - Roofing",
            date = referenceDate.plusDays(1),
            time = LocalTime.of(16, 0), 
            durationMin = 0,
            location = null,
            sourceType = "MS-TEAM", 
            provider = DutyProvider.MICROSOFT_365
        ).copy(acceptanceStatus = "PENDING"))


        // Recurring Work Pattern (M-F)
        for (i in 1..5) {
            val date = referenceDate.plusDays(i.toLong())
            if (date.dayOfWeek.value <= 5) { 
                duties.add(createEvent(
                    title = "Procurement Team Daily Standup",
                    date = date,
                    time = LocalTime.of(9, 0),
                    durationMin = 15,
                    location = LOC_WORK_FARMERS_BRANCH,
                    sourceType = "MS-CAL",
                    provider = DutyProvider.MICROSOFT_365
                ))
            }
        }

        // Family Event: Graduation
        duties.add(createEvent(
            title = "Son's Graduation Ceremony (UT Dallas)",
            date = referenceDate.plusDays(4), 
            time = LocalTime.of(10, 0),
            durationMin = 180,
            location = LOC_GRADUATION,
            sourceType = "GW-CAL",
            provider = DutyProvider.GOOGLE_WORKSPACE
        ))

        // Personal Task: Birthday
        duties.add(createTask(
            title = "Order Sarah's Birthday Cake",
            dueDate = referenceDate.plusDays(2),
            dueTime = LocalTime.of(17, 0),
            sourceType = "GW-TASK",
            provider = DutyProvider.GOOGLE_WORKSPACE
        ))
        
        // Weekend Trip
        duties.add(createTask(
            title = "Book Cabins for Broken Bow Trip",
            dueDate = referenceDate.plusDays(3),
            dueTime = LocalTime.of(20, 0),
            sourceType = "GW-TASK",
            provider = DutyProvider.GOOGLE_WORKSPACE
        ))

        return duties
    }

    private fun createEvent(
        title: String,
        date: LocalDate,
        time: LocalTime,
        durationMin: Long,
        location: String?,
        sourceType: String,
        provider: DutyProvider,
        virtualLink: String? = null // New parameter
    ): DutyEntity {
        val start = LocalDateTime.of(date, time)
        return DutyEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            startTime = start,
            endTime = start.plusMinutes(durationMin),
            location = location,
            provider = provider,
            providerDutyId = UUID.randomUUID().toString(),
            sourceType = sourceType,
            acceptanceStatus = "ACCEPTED", // Default
            virtualMeetingLink = virtualLink
        )
    }

    private fun createTask(
        title: String,
        dueDate: LocalDate,
        dueTime: LocalTime,
        sourceType: String,
        provider: DutyProvider
    ): DutyEntity {
        val due = LocalDateTime.of(dueDate, dueTime)
        return DutyEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            startTime = due, // For tasks, startTime often equals due time in this model or createdTime? 
            // In AliMinder Duty model, startTime is "Due Time" for tasks usually, or "Do Date".
            // Let's assume startTime = deadline for sorting purposes in the unified list.
            endTime = due,
            provider = provider,
            providerDutyId = UUID.randomUUID().toString(),
            sourceType = sourceType,
            acceptanceStatus = "ACCEPTED"
        )
    }
}
