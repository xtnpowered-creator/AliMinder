package com.aliminder.app.domain.model

data class UserSettings(
    val isFirstLaunch: Boolean = true,
    // Removed defaultPrepMinutes
    val defaultBufferMinutes: Int = 10,
    val audioRespectsSilentMode: Boolean = false,
    val audioVoiceSelection: String? = null,

    val urgencyTimeThreshold: Int = 60,
    val autoHideOverdueMinutes: Int = 120, // Default 2 hours
    val homeAddress: Address? = null,
    val workAddress: Address? = null
)
