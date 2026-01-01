package com.aliminder.app.presentation.screens.soundcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliminder.app.data.audio.VinylStackEngine
import com.aliminder.app.domain.model.PersonaStage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundCheckViewModel @Inject constructor(
    private val vinylEngine: VinylStackEngine
) : ViewModel() {

    // --- Direct pass-through from Engine ---
    val engineStatus: StateFlow<String> = vinylEngine.currentStage
    val isPlaying: StateFlow<Boolean> = vinylEngine.isPlaying
    val availableVoices: StateFlow<List<String>> = vinylEngine.availableVoices

    // --- UI-specific State ---
    private val _lastAction = MutableStateFlow("Ready")
    val lastAction: StateFlow<String> = _lastAction.asStateFlow()

    private val _selectedVoice = MutableStateFlow("Default")
    val selectedVoice: StateFlow<String> = _selectedVoice.asStateFlow()

    init {
        // Set initial voice once available
        viewModelScope.launch {
            availableVoices.collect { voices ->
                if (voices.isNotEmpty() && _selectedVoice.value == "Default") {
                    val defaultVoice = voices.first()
                    _selectedVoice.value = defaultVoice
                    vinylEngine.setVoice(defaultVoice)
                }
            }
        }
    }

    fun onVoiceSelected(voiceName: String) {
        _selectedVoice.value = voiceName
        vinylEngine.setVoice(voiceName)
        _lastAction.value = "Voice set to: $voiceName"
    }

    fun triggerPersona(stage: PersonaStage) {
        _lastAction.value = "Triggering ${stage.name}..."
        val stageName = when(stage) {
            PersonaStage.OPTIMISTIC -> "Optimistic"
            PersonaStage.WEARY -> "Weary"
            PersonaStage.URGENT -> "Urgent" // Fixed GRAVE -> URGENT
            PersonaStage.LATE -> "Late" // Added LATE
        }
        vinylEngine.playIntervention(stageName)
    }

    fun testPanicMute() {
        _lastAction.value = "Panic Mute Activated!"
        vinylEngine.panicMute()
    }
}
