# AliMinder: The Vigilance Sentinel
## Supplemental Specification v1.1

> [!NOTE]
> This document provides a granular breakdown of features outlined in the Master Specification. It focuses on the "how" of implementation, particularly for the audio engine, victory conditions, and testing harness.

---

## 1. The Vinyl Stack - Detailed Acoustic Profile

This section refines the "Aesthetic Friction" concept from the Master Spec, detailing the precise layering and timing of audio events.

### 1.1 The Refined 10-Second Prep Window

The "Silent Prep" is critical for battery efficiency and sensory impact. The sequence is as follows:

| Timestamp | Component | Logic | Persona Impact |
|---|---|---|---|
| **T+0.0s** | `AlarmManager` | Triggers a `BroadcastReceiver` to wake the CPU for a short-lived task. | Silent. |
| **T+0.1s** | TTS JIT Init | The native `TextToSpeech` engine begins its warm-up. This is the longest variable (`X`). | Silent. The "machine" is booting up. |
| **T+X+0.1s** | The "Thud" | `SoundPool` plays a randomized, low-frequency needle-drop sound. | The physical weight of the needle landing. |
| **T+X+0.1s** | Audio Ducking | Simultaneously, `requestAudioFocus` is called. Background media drops to ~20%. | The music drops *because* of the needle's impact. |
| **T+X+0.2s** | The "Sputter" | `SoundPool` plays 2-3 randomized high-frequency "dust pop" and "crackle" sounds. | Masks TTS initialization jitter and prevents habituation. |
| **T+X+0.8s** | The Hiss | `Media3/ExoPlayer` fades in a seamless "record groove" loop. | The "Spatial Anchor." AliMinder is now "in the room." |
| **T+X+1.1s** | The Stitch | The core audio payload is delivered: `[Human_Intro] -> [Gap] -> [TTS] -> [Gap] -> [Human_Outro]`. | The message is delivered. |
| **End+0.1s**| The "Clunk" | `SoundPool` plays a mechanical needle-lift sound. | The "machine" is disengaging. |
| **End+0.2s**| Cleanup | `tts.shutdown()` is called and audio focus is released. | Music returns to normal; battery is conserved. |

### 1.2 Anti-Habituation: The Randomization Layer

To prevent the user's brain from tuning out repetitive sounds, multiple audio elements are randomized during the `BroadcastReceiver`'s task.

- **Needle Drop Intro (`SoundPool`)**: Select one of **5+** unique `.wav` files, each containing a different thud, pop, and sputter pattern. This happens *before* the 10-second sequence begins.
- **The "Retro-Gap" (`Coroutine Delay`)**: The intentional silence between stitched audio clips is randomized between **150ms and 400ms**. This makes the speech feel less predictable.

### 1.3 The Layered Audio Asset Model

The "White Noise Floor" is composed of distinct, layered assets:

1.  **The Intro (Non-Looping)**: A 1.5s `.wav` file played by `SoundPool` containing the initial **Thud, Pops, and Sputter**.
2.  **The Loop (Seamless)**: A separate, seamless `groove_hiss.wav` file played by `Media3/ExoPlayer` that starts during the intro and continues under the speech.
3.  **The Outro (Non-Looping)**: A short `needle_lift.wav` file played by `SoundPool` to signal the end.

---

## 2. The Redemption Arc: "Victory & Praise" Logic

This system provides positive reinforcement and prevents user frustration by acknowledging when they comply with a nudge.

### 2.1 Victory Triggers

A "Victory" state is detected when, during a grooming window or before an event:
- The user closes a designated "Sin Group" app.
- A significant motion is detected via the accelerometer (e.g., standing up and walking).
- GPS data indicates the user has left a pre-defined geofence (e.g., "Home").

### 2.2 The "Record Scratch" Interruption

This is the most critical part of the redemption logic.

- **Scenario**: A "Weary" or "Grave" nag is currently playing.
- **Logic**: If a `Victory_Trigger` is detected *mid-speech*, the `VinylStackEngine` must:
    1.  Immediately stop all playing audio (TTS, hiss, human clips).
    2.  Play a sharp `scratch.wav` sound using `SoundPool`.
    3.  After a 200ms delay, play a short, surprised "Grudging Praise" clip.
- **Feedback**: This entire sequence is **audio-only**. No visual notifications are shown.

### 2.3 Grudging Praise Script Library

A separate pool of audio clips is used for victory events, categorized by the timing of the victory.

| Victory Type | Trigger | Example Audio Clip |
|---|---|---|
| **Early Victory** | User is ready before the first nag. | *"Oh. You're actually on schedule. I’ll just... put this needle back. Don't make me come out here again."* |
| **Standard Victory**| User complies after an "Optimistic" nag. | *"Finally. I was starting to think I'd have to talk to myself all morning. Good luck out there."* |
| **Late Redemption** | User complies after a "Grave" nag. | *"Better late than never, I suppose. Go. Save what's left of your reputation."* |

---

## 3. Developer & Hardware Auditing

A non-user-facing testing harness is required for tuning the audio experience. This will be implemented in the "Vinyl Lab" screen.

### 3.1 The "Vinyl Lab" Testing Harness

The `SoundCheckScreen` must be expanded to include:
- **Sliders/Input Fields** to adjust:
    - `jank_delay` (the "Retro-Gap" in ms).
    - `floor_volume` (the volume of the hiss loop).
    - `duck_ratio` (the percentage to lower background media).
- **A/B Toggling**: Buttons to switch between different `needle_drop.wav` and `groove_hiss.wav` assets.
- **Scenario Triggers**: A `BroadcastReceiver` (`com.aliminder.TRIGGER`) must be implemented to allow force-firing specific scenarios from an `adb` command for precise testing.
    - `adb shell am broadcast -a com.aliminder.TRIGGER --es distraction "TikTok" --ei stage 2`

### 3.2 Hardware Calibration Audits

#### Transient Audit (Pops & Crackles)
- **Goal**: Ensure high-frequency "pop" and "crackle" sounds cut through ambient noise without sounding like digital distortion.
- **Test**: Play the needle-drop sequence in a room with a running fan or air conditioner.
- **Fix**: If transients are inaudible, the source `.wav` files must be EQ'd to boost the **2kHz - 5kHz** frequency range.

#### Low-Frequency Audit (The "Thud")
- **Risk**: The low-frequency "thud" of the needle drop may be inaudible on budget phone speakers.
- **Test**: Play the sequence on a low-quality device.
- **Fix**: The `needle_drop.wav` files must be edited to layer a subtle, mid-range **"mechanical click"** on top of the bassy thud. This ensures the "weight" is felt even on small speakers.

### 3.3 Performance & Jitter Logging
The "Vinyl Lab" monitor must log the difference between `Target_Gap_ms` and `Actual_Gap_ms` during an audio stitch to verify that the OS is not introducing timing jitter that breaks the "janky" aesthetic.

---

## 4. Power Management Protocol (Android)

To meet the "Zero-Backend" and high-efficiency constraints:
- **No Persistent Service**: AliMinder will **not** use a persistent foreground service for monitoring.
- **The Lifecycle**:
    1.  `AlarmManager.setAlarmClock()` is used to schedule a wake-up. This is high-priority and will fire even in Doze mode.
    2.  The alarm triggers a `BroadcastReceiver`.
    3.  The `BroadcastReceiver` starts a short-lived background task (or `CoroutineWorker`) that executes the 10-second audio prep and playback sequence.
    4.  The task and all components (`TTS`, `ExoPlayer`) are terminated immediately after completion.
- **Motion Sampling**: Use the `Significant Motion Sensor` to detect large movements (like standing up), polling at a low frequency (1Hz) only when a grooming window is active.

---

## 5. Refined "Grave" Persona Scripts

The "Grave" stage is not just about being late; it's about connecting the user's current actions to their pre-defined fears.

- **The "Social Friction" Script**:
  > *[Hiss] ... "I've stopped the timer. There's no point. In twenty minutes, you'll be walking into the ... [TTS: Staff Meeting] ... and everyone will look at their watches. I hope that ... [TTS: Reddit] ... thread was worth the look on their faces."*

- **The "Personal Chaos" Script**:
  > *[Hiss] ... "We've officially entered the 'Frantic' zone. You're going to forget your keys. You're going to hit every red light. You are choosing to have a stressful morning. I'm just here to watch it happen."*

---

## 6. Required Audio Assets Checklist

- [ ] **Human Clips**: 10-15 intro/outro clips, categorized by persona.
- [ ] **Needle Drops**: 5+ unique `needle_drop_X.wav` files (thud + pops).
- [ ] **Record Scratch**: 1 `scratch.wav` for interruptions.
- [ ] **Groove Hiss**: 1 seamless `groove_hiss.wav` loop.
- [ ] **Needle Lift**: 1 `needle_lift.wav` mechanical clunk.
