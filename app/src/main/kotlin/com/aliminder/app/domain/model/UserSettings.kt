package com.aliminder.app.domain.model

data class UserSettings(
    val isFirstLaunch: Boolean = true,
    val defaultPrepMinutes: Int = 15,
    val defaultBufferMinutes: Int = 10,
    val audioRespectsSilentMode: Boolean = false,
    val audioVoiceSelection: String? = null,
    val useDynamicTitleBarColor: Boolean = true,
    val urgencyTimeThreshold: Int = 60,
    val autoHideOverdueMinutes: Int = 120, // Default 2 hours
    val homeAddress: Address? = null,
    val workAddress: Address? = null
)
