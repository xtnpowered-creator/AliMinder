package com.aliminder.app.presentation.mock

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/**
 * Mock data models for UI development.
 * These will be replaced with real domain models in Phase 2.
 */

enum class PersonaStage {
    OPTIMISTIC,  // T-minus 30m+
    WEARY,       // T-minus 15-30m
    GRAVE        // Past PoNR
}

data class MockEvent(
    val id: String,
    val title: String,
    val startTime: LocalDateTime,
    val commuteMinutes: Int,
    // prepMinutes removed
    val bufferMinutes: Int,
    val category: String = "Work",
    val source: String = "Shadow" // Shadow, M365, Google
) {
    val ponr: LocalDateTime
        get() = startTime
            .minusMinutes(commuteMinutes.toLong())
            .minusMinutes(bufferMinutes.toLong())
    
    val deltaMinutes: Long
        get() {
            val now = LocalDateTime.now()
            return java.time.Duration.between(now, ponr).toMinutes()
        }
    
    val personaStage: PersonaStage
        get() = when {
            deltaMinutes >= 30 -> PersonaStage.OPTIMISTIC
            deltaMinutes in 0..29 -> PersonaStage.WEARY
            else -> PersonaStage.GRAVE
        }
}

data class MockSinGroupApp(
    val packageId: String,
    val name: String,
    val gracePeriodSeconds: Int = 60,
    val energyLevel: String = "High" // High, Medium, Low
)

data class MockRepercussion(
    val id: String,
    val text: String,
    val gravityScore: Int, // 1-10
    val category: String, // Professional, Social, Financial, Personal
    val contextTags: List<String> = emptyList()
)

/**
 * Sample events with varying urgency for UI testing
 */
object MockData {
    
    val sampleEvents = listOf(
        // GRAVE - Already late
        MockEvent(
            id = "1",
            title = "Team Stand-up",
            startTime = LocalDateTime.now().minusMinutes(10),
            commuteMinutes = 0,

            bufferMinutes = 5,
            category = "Meeting"
        ),
        
        // WEARY - 20 minutes until PoNR
        MockEvent(
            id = "2",
            title = "1:1 with Manager",
            startTime = LocalDateTime.now().plusMinutes(45),
            commuteMinutes = 15,

            bufferMinutes = 5,
            category = "Meeting"
        ),
        
        // OPTIMISTIC - 50 minutes until PoNR
        MockEvent(
            id = "3",
            title = "Dentist Appointment",
            startTime = LocalDateTime.now().plusHours(2),
            commuteMinutes = 25,

            bufferMinutes = 15,
            category = "Personal"
        ),
        
        // OPTIMISTIC - 2+ hours away
        MockEvent(
            id = "4",
            title = "Client Presentation",
            startTime = LocalDateTime.now().plusHours(3),
            commuteMinutes = 30,

            bufferMinutes = 10,
            category = "Meeting"
        ),
        
        // WEARY - 10 minutes until PoNR
        MockEvent(
            id = "5",
            title = "Lunch with Sarah",
            startTime = LocalDateTime.now().plusMinutes(35),
            commuteMinutes = 15,

            bufferMinutes = 5,
            category = "Social"
        ),

        // TASKS
        MockEvent(
            id = "6",
            title = "Submit Weekly Report",
            startTime = LocalDateTime.now().plusHours(4),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Task"
        ),
        MockEvent(
            id = "7",
            title = "Buy Groceries",
            startTime = LocalDateTime.now().plusHours(5),
            commuteMinutes = 15,

            bufferMinutes = 0,
            category = "Task"
        ),

        // PENDING INVITES
        MockEvent(
            id = "8",
            title = "Project Kickoff (Invite)",
            startTime = LocalDateTime.now().plusDays(1).plusHours(2),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Pending"
        ),
        MockEvent(
            id = "9",
            title = "Happy Hour (Invite)",
            startTime = LocalDateTime.now().plusDays(2).plusHours(6),
            commuteMinutes = 20,

            bufferMinutes = 0,
            category = "Pending"
        ),
        
        // --- NEW MOCK DATA ---
        
        // Tomorrow Morning - Optimistic
        MockEvent(
            id = "10",
            title = "Morning Gym Session",
            startTime = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0),
            commuteMinutes = 15,

            bufferMinutes = 5,
            category = "Personal"
        ),
        
        // Tomorrow Work - Optimistic
        MockEvent(
            id = "11",
            title = "Code Review Meeting",
            startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(30),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Meeting"
        ),
        
        // Tomorrow Afternoon - Task
        MockEvent(
            id = "12",
            title = "Update Jira Tickets",
            startTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Task"
        ),
        
        // Late Tonight - Optimistic
        MockEvent(
            id = "13",
            title = "Dinner Prep",
            startTime = LocalDateTime.now().plusHours(6),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Task"
        ),
        
        // Tomorrow Evening - Social
        MockEvent(
            id = "14",
            title = "Movie Night with Friends",
            startTime = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0),
            commuteMinutes = 25,

            bufferMinutes = 10,
            category = "Social"
        ),
        
        // Early Morning - Weary/Urgent potential if accessed late at night
        MockEvent(
            id = "15",
            title = "Flight Check-in",
            startTime = LocalDateTime.now().plusDays(1).withHour(6).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Task"
        ),
        
        // Work Block - Optimistic
        MockEvent(
            id = "16",
            title = "Deep Work: Architecture",
            startTime = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Work"
        ),
        
        // Short Notice - Weary
        MockEvent(
            id = "17",
            title = "Quick Sync",
            startTime = LocalDateTime.now().plusHours(1).plusMinutes(15),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Meeting"
        ),
        
        // Tomorrow - Pending
        MockEvent(
            id = "18",
            title = "Doctor Follow-up (Tentative)",
            startTime = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0),
            commuteMinutes = 20,

            bufferMinutes = 5,
            category = "Pending"
        ),
        
        // Day after tomorrow - Optimistic
        MockEvent(
            id = "19",
            title = "Quarterly Planning",
            startTime = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0),
            commuteMinutes = 30,

            bufferMinutes = 10,
            category = "Meeting"
        ),
        
        // Day after tomorrow - Task
        MockEvent(
            id = "20",
            title = "Pay Utility Bills",
            startTime = LocalDateTime.now().plusDays(2).withHour(12).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Task"
        ),
        
        // Near Future - Task
        MockEvent(
            id = "21",
            title = "Water Plants",
            startTime = LocalDateTime.now().plusHours(3).plusMinutes(30),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Task"
        ),
        
        // Tomorrow - Social
        MockEvent(
            id = "22",
            title = "Coffee with Mentor",
            startTime = LocalDateTime.now().plusDays(1).withHour(8).withMinute(30),
            commuteMinutes = 15,

            bufferMinutes = 5,
            category = "Social"
        ),
        
        // Day after tomorrow - Personal
        MockEvent(
            id = "23",
            title = "Grocery Shopping",
            startTime = LocalDateTime.now().plusDays(2).withHour(17).withMinute(30),
            commuteMinutes = 10,

            bufferMinutes = 0,
            category = "Task"
        ),
        
        // Tomorrow - Work
        MockEvent(
            id = "24",
            title = "Team Retro",
            startTime = LocalDateTime.now().plusDays(1).withHour(16).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Meeting"
        ),
        
        // Day after tomorrow - Optimistic
        MockEvent(
            id = "25",
            title = "Workshop: Kotlin Flows",
            startTime = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Work"
        ),
        
        // Tonight - Task
        MockEvent(
            id = "26",
            title = "Journaling",
            startTime = LocalDateTime.now().plusHours(8),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Personal"
        ),
        
        // Tomorrow - Pending
        MockEvent(
            id = "27",
            title = "Lunch & Learn (Invite)",
            startTime = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Pending"
        ),
        
        // Day after tomorrow - Social
        MockEvent(
            id = "28",
            title = "Game Night",
            startTime = LocalDateTime.now().plusDays(2).withHour(20).withMinute(0),
            commuteMinutes = 20,

            bufferMinutes = 0,
            category = "Social"
        ),
        
        // Late tomorrow - Task
        MockEvent(
            id = "29",
            title = "Prepare Presentation Deck",
            startTime = LocalDateTime.now().plusDays(1).withHour(21).withMinute(0),
            commuteMinutes = 0,

            bufferMinutes = 0,
            category = "Task"
        )
    )
    
    val sampleSinGroupApps = listOf(
        MockSinGroupApp(
            packageId = "com.reddit.frontpage",
            name = "Reddit",
            energyLevel = "Medium"
        ),
        MockSinGroupApp(
            packageId = "com.instagram.android",
            name = "Instagram",
            energyLevel = "High"
        ),
        MockSinGroupApp(
            packageId = "com.zhiliaoapp.musically",
            name = "TikTok",
            energyLevel = "High"
        )
    )
    
    val sampleRepercussions = listOf(
        MockRepercussion(
            id = "1",
            text = "Boss's visible disappointment",
            gravityScore = 9,
            category = "Professional",
            contextTags = listOf("work", "authority", "career")
        ),
        MockRepercussion(
            id = "2",
            text = "Missing out on promotion discussion",
            gravityScore = 8,
            category = "Professional",
            contextTags = listOf("career", "advancement")
        ),
        MockRepercussion(
            id = "3",
            text = "Friends waiting in awkward silence",
            gravityScore = 7,
            category = "Social",
            contextTags = listOf("friends", "social", "embarrassment")
        ),
        MockRepercussion(
            id = "4",
            text = "Late fee charged to account",
            gravityScore = 6,
            category = "Financial",
            contextTags = listOf("money", "fees")
        ),
        MockRepercussion(
            id = "5",
            text = "Breaking promise to myself again",
            gravityScore = 8,
            category = "Personal",
            contextTags = listOf("self-improvement", "integrity")
        )
    )
    
    // Helper to format time for display
    fun formatTime(time: LocalDateTime): String {
        return time.format(DateTimeFormatter.ofPattern("h:mm a"))
    }
    
    // Helper to format delta display
    fun formatDelta(minutes: Long): String {
        val absMinutes = abs(minutes)
        val days = absMinutes / (24 * 60)
        val remainingMinutes = absMinutes % (24 * 60)
        val hours = remainingMinutes / 60
        val mins = remainingMinutes % 60
        
        return if (minutes >= 0) {
            // Future
            if (days > 0) {
                String.format(Locale.US, "%d days\n%02d:%02d", days, hours, mins)
            } else {
                String.format(Locale.US, "%02d:%02d", hours, mins)
            }
        } else {
            // Late (Negative) - No days for late items, as they are filtered out
            String.format(Locale.US, "RUSH\n%02d:%02d", hours, mins)
        }
    }

    // Helper to format delta display with PersonaStage awareness
    fun formatDelta(minutes: Long, stage: com.aliminder.app.domain.model.PersonaStage): String {
        val absMinutes = abs(minutes)
        val days = absMinutes / (24 * 60)
        val remainingMinutes = absMinutes % (24 * 60)
        val hours = remainingMinutes / 60
        val mins = remainingMinutes % 60

        // If it is URGENT, we want to show countdown only (no RUSH text)
        if (stage == com.aliminder.app.domain.model.PersonaStage.URGENT) {
             return String.format(Locale.US, "%02d:%02d", hours, mins)
        }
        
        // If it is LATE, we want to show LATE only (no countdown)
        if (stage == com.aliminder.app.domain.model.PersonaStage.LATE) {
             return "LATE"
        }

        return if (minutes >= 0) {
            // Future (Optimistic / Weary)
            if (days > 0) {
                String.format(Locale.US, "%d days\n%02d:%02d", days, hours, mins)
            } else {
                String.format(Locale.US, "%02d:%02d", hours, mins)
            }
        } else {
             // Fallback for negative delta if not caught above (though Late usually catches this)
             "LATE"
        }
    }
}
