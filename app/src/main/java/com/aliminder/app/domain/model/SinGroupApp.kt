package com.aliminder.app.domain.model

/**
 * Energy level of a Sin Group app (used for "Irony Match" logic).
 */
enum class EnergyLevel {
    /** High-energy apps (TikTok, Instagram Reels) */
    HIGH,
    
    /** Medium-energy apps (Twitter, Reddit browsing) */
    MEDIUM,
    
    /** Low-energy apps (Reading articles, passive consumption) */
    LOW
}

/**
 * Sin Group app configuration.
 * 
 * Represents a "Time-Sink" app that AliMinder monitors for digital stasis.
 * User-configured during onboarding or via Sin Group management screen.
 */
data class SinGroupApp(
    /** App package name (e.g., "com.instagram.android") */
    val packageName: String,
    
    /** User-friendly app name (e.g., "Instagram") */
    val displayName: String,
    
    /** Energy level for contextual matching */
    val energyLevel: EnergyLevel,
    
    /** Grace period in seconds before triggering intervention */
    val gracePeriodSeconds: Int = 60,
    
    /**Whether monitoring is currently enabled for this app */
    val isEnabled: Boolean = true,
    
    /** Category (social media, games, video, etc.) */
    val category: String = "uncategorized"
) {
    companion object {
        /** Default grace period (60 seconds) */
        const val DEFAULT_GRACE_PERIOD = 60
    }
}
