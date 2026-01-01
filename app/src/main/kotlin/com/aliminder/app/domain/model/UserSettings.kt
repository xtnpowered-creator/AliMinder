package com.aliminder.app.domain.model

data class UserSettings(
    val isFirstLaunch: Boolean = true,
    val defaultCommuteMinutes: Int = 20,
    val defaultPrepMinutes: Int = 15,
    val defaultBufferMinutes: Int = 10,
    val audioRespectsSilentMode: Boolean = false,
    val audioVoiceSelection: String? = null,
    val useDynamicTitleBarColor: Boolean = true // The new setting
)
