package com.aliminder.app.data.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.aliminder.app.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Audio Engine ("The Vinyl Stack").
 * Manages the aesthetic friction, needle drops, and audio degradation.
 */
@Singleton
class VinylStackEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope // Injected CoroutineScope
) : TextToSpeech.OnInitListener {

    // --- Public State Flows ---
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentStage = MutableStateFlow("Idle")
    val currentStage: StateFlow<String> = _currentStage.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<String>>(emptyList())
    val availableVoices: StateFlow<List<String>> = _availableVoices.asStateFlow()

    // --- Internal State ---
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        _currentStage.value = "Initializing TTS..."
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                _currentStage.value = "Error: US Language not supported"
                Log.e("VinylStackEngine", "The Language specified is not supported!")
            } else {
                isTtsReady = true
                _currentStage.value = "Idle"
                // Now that TTS is ready, populate the voices list
                _availableVoices.value = tts?.voices
                    ?.filter { 
                        // Filter for US English voices only
                        it.locale.language == "en" && 
                        it.locale.country == "US"
                    }
                    ?.map { it.name }
                    ?.sorted()
                    ?: emptyList()
            }
        } else {
            _currentStage.value = "Error: TTS Failed to start"
            Log.e("VinylStackEngine", "Initialization Failed!")
        }
    }

    fun setVoice(voiceName: String) {
        if (!isTtsReady) return
        val voice = tts?.voices?.find { it.name == voiceName }
        if (voice != null) {
            tts?.voice = voice
        } else {
            Log.w("VinylStackEngine", "Could not find voice: $voiceName")
        }
    }

    fun playIntervention(personaStage: String) {
        if (_isPlaying.value) return

        scope.launch {
            _isPlaying.value = true
            
            // 1. Silent Wake / Prep (4 seconds)
            _currentStage.value = "T+0s: Silent Wake & Prep"
            delay(4000) 

            // 2. Audio Ducking (at T+4s)
            _currentStage.value = "T+4s: Audio Ducking (Music lowers)"
            delay(100)

            // 3. Needle Drop (at T+4.1s)
            _currentStage.value = "T+4.1s: *THUD* (Needle Drop)"
            // TODO: Play SoundPool thud
            delay(500)

            // 4. White Noise Fade In (finishes at T+5s)
            _currentStage.value = "T+4.6s: Hiss Fade In (Vinyl Loop)"
            // TODO: Start ExoPlayer loop
            delay(400)

            // 5. Speech (at T+5s)
            _currentStage.value = "T+5s: \"$personaStage\" Intervention Playing..."
            
            val message = getPersonaMessage(personaStage)
            if (isTtsReady) {
                val speechRate = when (personaStage) {
                    "Optimistic" -> 1.0f
                    "Weary" -> 0.85f
                    "Grave" -> 0.75f
                    else -> 1.0f
                }
                tts?.setSpeechRate(speechRate)
                
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "InterventionID")
                
                val baseCharsPerSecond = 15.0
                val estimatedDuration = ((message.length / baseCharsPerSecond) * 1000 * (1f / speechRate)).toLong()
                
                delay(estimatedDuration + 500) // Add buffer
            } else {
                _currentStage.value = "Error: TTS not ready for playback"
                delay(2000)
            }

            // 6. Cleanup
            _currentStage.value = "T+End: Fade Out & Restore Volume"
            delay(1000)
            
            _currentStage.value = "Idle"
            _isPlaying.value = false
        }
    }
    
    private fun getPersonaMessage(stage: String): String {
        return when (stage) {
            "Optimistic" -> "I see we're looking at Reddit instead of the Staff Meeting. Bold choice."
            "Weary" -> "Again with the Instagram? I'm literally exhausted for you."
            "Grave" -> "It's over. You're late. Everyone is currently wondering where you are."
            else -> "This is a test of the AliMinder Vigilance System."
        }
    }
    
    fun panicMute() {
        _currentStage.value = "PANIC MUTE TRIGGERED"
        if (isTtsReady) {
            tts?.stop()
        }
        _isPlaying.value = false
    }
}
