package com.aliminder.app.domain.model

import java.time.LocalDateTime

/**
 * Provider source for duties (events and tasks).
 */
enum class DutyProvider {
    /** Microsoft 365 Graph API */
    MICROSOFT_365,
    
    /** Google Workspace Calendar API */
    GOOGLE_WORKSPACE,
    
    /** AliMinder Shadow Calendar (local-only) */
    SHADOW
}

/**
 * Unified duty from any provider (M365, Google, or Shadow).
 * 
 * This domain model aggregates duties from multiple sources into a single
 * stream sorted by PoNR proximity for the "ALL" dashboard view.
 */
data class Duty(
    /** Unique identifier (provider-specific) */
    val id: String,
    
    /** Duty title */
    val title: String,
    
    /** Duty description */
    val description: String? = null,
    
    /** Duty start time (or due time for tasks) */
    val startTime: LocalDateTime,
    
    /** Duty end time */
    val endTime: LocalDateTime,
    
    /** Location/venue */
    val location: String? = null,
    
    /** Provider source */
    val provider: DutyProvider,
    
    /** Custom commute time (minutes) - overrides default */
    val customCommuteMinutes: Int? = null,
    
    /** Custom prep time (minutes) - overrides default */
    val customPrepMinutes: Int? = null,
    
    /** Custom buffer (minutes) - overrides default */
    val customBufferMinutes: Int? = null,
    
    /** Category/tag for filtering */
    val category: String? = null,
    
    /** PoNR calculation result (computed) */
    val ponr: PoNRCalculation? = null,
    
    /** Delta minutes (computed from PoNR) */
    val delta: Int = Int.MAX_VALUE,
    
    /** Whether this is an all-day duty */
    val isAllDay: Boolean = false,
    
    /** Whether duty has been dismissed/snoozed */
    val isDismissed: Boolean = false
) {
    /**
     * Returns the persona stage for this duty based on current delta.
     */
    fun getPersonaStage(): PersonaStage {
        return ponr?.personaStage ?: PersonaStage.OPTIMISTIC
    }
    
    /**
     * Returns the effective commute minutes (custom or default).
     */
    fun getEffectiveCommuteMinutes(defaultMinutes: Int): Int {
        return customCommuteMinutes ?: defaultMinutes
    }
    
    /**
     * Returns the effective prep minutes (custom or default).
     */
    fun getEffectivePrepMinutes(defaultMinutes: Int): Int {
        return customPrepMinutes ?: defaultMinutes
    }
    
    /**
     * Returns the effective buffer minutes (custom or default).
     */
    fun getEffectiveBufferMinutes(defaultMinutes: Int): Int {
        return customBufferMinutes ?: defaultMinutes
    }
}
