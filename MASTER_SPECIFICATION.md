# AliMinder: The Vigilance Sentinel
## Master Technical Specification v1.0

> [!IMPORTANT]
> **The Rigor Protocol**: This specification adheres to Audited Uncertainty, Unfiltered Analysis, Zero-Backend Architecture, and Conceptual Audit standards. All mechanisms detailed herein are structural requirements for implementation.

---

## 1. EXECUTIVE SUMMARY

**Project Name**: AliMinder  
**Core Purpose**: A high-vigilance, persona-driven assistant for neurodivergent users (mild ADHD) to prevent "Time Blindness" during grooming windows and in-office corporate environments.

**Philosophy**: Zero-Backend, Aesthetic Friction (Needle-Drop/Jank), Contextual Sarcasm, and Dynamic Persona Evolution.

**Platform**: Cross-platform mobile application (Android/iOS) with native implementations.

**Core Principle**: AliMinder is a **character, not a tool**. Its tone, audio aesthetics, and intervention strategies evolve based on proximity to the Point of No Return (PoNR).

---

## 2. IDENTITY & PERSONA: "WITTY-TO-WEARY"

AliMinder's personality shifts dynamically based on urgency, creating an emotional arc that mirrors the user's descent from "in control" to "objectively late."

### 2.1 The Three Persona States

#### Stage 1: Optimistic (T-Minus 30m)
- **Tone**: Brief, helpful, slightly snarky
- **Audio Aesthetic**: Clean needle drops, moderate pacing
- **Example**: *"I see we're looking at [Reddit] instead of the [Staff Meeting]. Bold choice."*

#### Stage 2: Weary (T-Minus 15m)
- **Tone**: Disappointed, audibly exhausted
- **Audio Aesthetic**: Slower pacing, deeper "Record RPM" sound, audible sighs
- **Narrative**: Disappointed human clips + robotic Mad-Libs
- **Example**: *"Again with the [Instagram]? I'm literally exhausted for you."*

#### Stage 3: Grave (Past PoNR)
- **Tone**: No more advice; only descriptions of unfolding consequences
- **Audio Aesthetic**: Wow and Flutter degradation, pitch instability
- **Narrative**: Direct focus on social/professional repercussions
- **Example**: *"It's over. You're late. Everyone is currently wondering where you are. I hope [Twitter] was worth it."*

### 2.2 The Narrative Mechanism

**Stitched Audio Mad-Libs**: AliMinder combines pre-recorded human audio with native TTS to create contextually specific interventions:

```
[Human_Intro.wav] → [250ms Gap] → [Native_TTS: "Reddit"] → [250ms Gap] → [Human_Outro.wav]
```

The **intentional gaps** create aesthetic friction—the audio feels "assembled" rather than polished, maximizing pattern-interruption.

---

## 3. DYNAMIC SCRIPTING: THE SOCIAL MIRROR

> [!NOTE]
> **New Feature**: Human_Intro and Human_Outro selection is now driven by contextual matching algorithms.

### 3.1 The "Human Frame" Taxonomy

Audio clips are categorized and paired based on:

1. **Sin Group Category**: The type of distraction detected
2. **User's Hardwired Fears**: Personal repercussions defined during setup
3. **Persona Stage**: Optimistic/Weary/Grave

### 3.2 Contextual Matching Logic

#### The "Irony" Match
- **High-Energy Sin Group** (TikTok, Instagram Reels) → **Slow/Weary Intro**
- Creates sharp tonal contrast to break immersion
- Example: Detecting TikTok triggers a slow, disappointed sigh intro

#### The "Fear" Variable
- **Grave Stage Outros** are pulled from the user's **Personal Repercussion Library**
- User-defined during setup: "Boss's disapproval," "Missed promotion," "Late fees," "Social embarrassment"
- AliMinder selects the most contextually relevant fear based on:
  - Calendar event type (1:1 meeting → "Boss's disapproval")
  - Time of day (morning grooming → "Traffic consequences")
  - Location context (office geofence → "Professional reputation")

### 3.3 Hardwired Fears Configuration

Users define their specific anxieties during onboarding:

- **Professional**: Boss's face, team disappointment, career impact
- **Social**: Friends waiting, party missed, social credibility
- **Financial**: Late fees, missed opportunities, monetary loss
- **Personal**: Self-disappointment, broken promises, routine failure

Each fear has associated:
- **5-10 Human Outro clips** (varying intensity)
- **Gravity Score** (1-10, user-defined)
- **Context Tags** (work, social, personal, financial)

---

## 4. THE POINT OF NO RETURN (PoNR) ALGORITHM

> [!IMPORTANT]
> **Critical Feature**: The PoNR is calculated dynamically using live deltas, not static reminders.

### 4.1 The Formula

```
PoNR = MeetingTime - (CommuteTime + Buffer + GroomingRequirement)
```

**Components**:
- **MeetingTime**: Pulled from synced calendar (M365 or Google)
- **CommuteTime**: User-defined per location or calendar location field
- **Buffer**: Safety margin (user-defined, typically 5-10 minutes)
- **GroomingRequirement**: Context-specific prep time (shower, dress, pack bag)

### 4.2 The Negative Delta

AliMinder tracks the **"Negative Space"**: minutes remaining before the user is objectively unable to arrive on time.

```
Delta = PoNR - CurrentTime
```

**Persona Triggers**:
- `Delta ≥ 30m` → **Optimistic** (gentle nudges)
- `30m > Delta ≥ 15m` → **Weary** (escalating concern)
- `Delta ≤ 0m` → **Grave** (consequences-focused)

### 4.3 Live Recalculation

- **Every 60 seconds**: Recalculate Delta while Sin Group app is active
- **Every 5 minutes**: Recalculate Delta during normal operation
- **Immediate trigger**: If Delta crosses stage threshold

---

## 5. CORE AUDIO ENGINE: "THE VINYL STACK"

The audio architecture avoids "professional polish" in favor of **Aesthetic Friction** to maximize attention capture.

### 5.1 The 10-Second Prep Window (Battery Efficient)

Asynchronous, system-clock scheduled timing (no callbacks):

| Time | Event | Description |
|------|-------|-------------|
| **T+0s** | Silent Wake | App wakes via `setAlarmClock` (Android) or `BackgroundTasks` (iOS) |
| **T+0s** | TTS Init | Native TTS engine begins initialization |
| **T+9s** | Audio Ducking | Media volume drops to 20% |
| **T+9.1s** | Needle Drop | Randomized "Thud + Sputters + Dust Pops" (5+ variants) |
| **T+9.1s** | Noise Floor Fade-In | Record groove hiss loop begins (1s fade) |
| **T+10s** | The Stitch | `[Human_Intro] → [250ms Gap] → [TTS Variable] → [250ms Gap] → [Human_Outro]` |
| **T+[End]+1s** | Noise Floor Fade-Out | Record hiss fades out over 1s |
| **T+[End]+2s** | TTS Shutdown | Engine terminated to prevent RAM/battery bleed |

### 5.2 Anti-Habituation Randomization

> [!TIP]
> Variance prevents users from "timing" the interruption and tuning it out.

**Variable Needle Landing**:
- After the needle "thud," the hiss duration before speech varies
- Range: **0.5s to 1.5s** (randomized per intervention)
- User cannot predict exact speech start time

**Needle Drop Variants**:
- Minimum **5 distinct** thud + pop patterns
- Rotated randomly to prevent pattern recognition
- Each variant: 200-400ms duration

### 5.3 The White Noise Floor

**Purpose**: Create a "Spatial Anchor" that signals "AliMinder is speaking now."

**Implementation**:
- Seamless **Record Groove Loop** (authentic vinyl surface noise)
- Fade-in: 1s (begins at T+9.1s)
- Playback: Throughout entire speech duration
- Fade-out: 1s (begins after final word)

**Audio Properties**:
- Frequency response: Warm, low-pass filtered
- Volume: -18dB relative to speech
- Loop point: Seamless (no clicks at boundary)

### 5.4 Audio Focus & Priority

- **Stream Type**: `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` (Android) / `AVAudioSessionCategoryPlayback` with `mixWithOthers: false` (iOS)
- **Priority**: **High-priority "Alarm Stream"** to bypass silent mode
- **User Configurable**: Option to respect silent mode (default: ignore)
- **Ducking Behavior**: Music/podcasts drop to 20%, not pause

---

## 6. AUDIO DEGRADATION: WOW AND FLUTTER

> [!NOTE]
> **New Feature**: Stage 3 (Grave) implements mechanical degradation effects.

### 6.1 The Degradation Effect

As the user gets **later** (Delta becomes more negative), the audio playback experiences:

- **Pitch Shifting**: ±0.5 to 2 semitones
- **Speed Instability**: 95% to 105% playback speed
- **Wow** (slow fluctuations): 0.2-0.5 Hz modulation
- **Flutter** (fast fluctuations): 5-10 Hz modulation

### 6.2 Implementation

**Method**: Apply real-time pitch and speed modulation to:
- The "Record Groove" white noise floor
- The Human_Intro and Human_Outro clips
- (Optional) The TTS output if platform supports

**Progression**:
```
Delta = 0 (Just late):      No degradation
Delta = -5m:                Subtle wow (0.2 Hz, ±0.5 semitone)
Delta = -10m:               Moderate flutter (7 Hz, ±1 semitone)
Delta = -15m+:              Heavy degradation (±2 semitones, 95-105% speed)
```

**Rationale**: Signals that the "Machine" (AliMinder) is symbolically failing along with the user's schedule—the record player is "dying."

---

## 7. THE PHYSICAL KILL-SWITCH: PANIC MUTE

> [!CAUTION]
> **Critical Feature**: Instant audio abort for corporate environments.

### 7.1 The Fail-Safe Mechanism

**Trigger Gestures**:
1. **Flip-to-Shhh**: Flip device face-down (accelerometer detection)
2. **Proximity Wave**: Wave hand over proximity sensor

**Behavior**:
- **Instant Audio Kill**: Abort all AliMinder audio immediately
- **Media Restoration**: Restore previous media to 100% volume
- **No Notification**: Silent abort (no toast, no visual feedback)
- **Cooldown**: 5-minute silence period before next intervention

### 7.2 Context Awareness

**Corporate Environment Detection**:
- Office geofence active, OR
- Connected to corporate Wi-Fi SSID, OR
- Calendar shows "In Meeting" status

**Activation**:
- Kill-switch is **always active** but prioritized in corporate contexts
- Outside office: User can configure to disable flip-to-mute

### 7.3 Implementation Requirements

**Android**:
- `Sensor.TYPE_ACCELEROMETER` (flip detection)
- `Sensor.TYPE_PROXIMITY` (wave detection)
- Threshold: 45° flip angle, proximity < 5cm

**iOS**:
- `CMMotionManager` (accelerometer)
- Proximity sensor via `UIDevice.proximityState`

---

## 8. DISTRACTION HEURISTICS: THE "ALWAYS-ON" EYE

Detection is **persistent** and **contextually aware**, requiring no manual toggles.

### 8.1 Sin Group Logic

**Definition**: User-defined list of "Time-Sink" App IDs.

**Categories**:
- **Social Media**: Instagram, TikTok, Snapchat, Twitter/X
- **Endless Scroll**: Reddit, Pinterest, news aggregators
- **Games**: Mobile games, puzzle apps
- **Video**: YouTube, Twitch, streaming apps

**User Configuration**:
- Add apps via App ID picker
- Assign "Energy Level" (High/Medium/Low) for Irony Match
- Set per-app "Grace Period" (default: 60s)

### 8.2 Digital Stasis Detection

**Logic**:
```
IF [Foreground_App == Sin_Group]
AND [Accelerometer == No_Movement > 60s]
AND [Screen == ON]
THEN Queue Nag
```

**Accelerometer Threshold**:
- < 0.05 m/s² movement for continuous 60s period
- Detects "phone resting on table" or "zombie scrolling" posture

### 8.3 Corporate Context Cross-Referencing

**Enhanced Logic**:
```
IF [Digital_Stasis == TRUE]
AND [CurrentTime within 15m of PoNR]
AND [Location == Office OR Calendar == Next_Event]
THEN Escalate to Weary/Grave Stage
```

**Meeting Proximity**:
- **30m before**: Optimistic nudge
- **15m before**: Weary escalation
- **Past PoNR**: Immediate Grave

---

## 9. DATA INTEGRATION & ECOSYSTEM SYNC

> [!IMPORTANT]
> **Zero-Backend Constraint**: All calendar data, tokens, and user data remain **on-device only**.

### 9.1 Provider Integration

**Supported Ecosystems**:
1. **Microsoft 365** (Graph API)
2. **Google Workspace** (Calendar API)

**Authorization**:
- **OAuth2 with PKCE** (Proof Key for Code Exchange)
- **Local-Only Token Management**: Tokens stored in device keychain
- **No Server Component**: Direct device-to-provider communication
- **Token Refresh**: Automatic refresh using refresh tokens (no backend)

### 9.2 The "Shadow" Calendar

**Purpose**: AliMinder-specific vigilance events that don't exist in native ecosystems.

**Features**:
- **Layered Reminders**: Additional PoNR-based alerts
- **Grooming Windows**: Morning prep routines not in corporate calendar
- **Custom Buffers**: Per-event commute and prep overrides
- **Local Storage**: Encrypted on-device database

**Use Cases**:
- "Start getting ready for 9am meeting" (7:30am vigilance event)
- "Leave for dentist appointment" (custom commute calculation)
- "Begin evening shutdown routine" (personal productivity)

### 9.3 Device Migration: The "QR-Handshake" Protocol

> [!NOTE]
> **Zero-Backend Constraint**: Cross-platform device migration (Android ↔ iOS) must occur without intermediary servers using local encrypted payload transfer.

#### 9.3.1 The Migration Payload

**Contents**:
- **Shadow Calendar**: All AliMinder-only reminders and vigilance events
- **Repercussion Library**: Complete Hardwired Fears configuration
  - Fear text, gravity scores, context tags
  - Associated outro clip preferences
- **Sin Group Configurations**: App IDs, energy levels, grace periods
- **Personality Config**: 
  - Default PoNR parameters (commute, prep, buffer)
  - Audio preferences (volume, alarm stream settings)
  - UI preferences
- **Provider Metadata**: Calendar sync preferences (OAuth requires re-authentication)
- **Intervention History**: Last 30 days of anonymized logs (optional)

**Format**:
- **Primary**: Protocol Buffers (`.proto`) for efficient binary serialization
- **Fallback**: Encrypted JSON for human readability during debugging
- **File Extension**: `.aliminder` or `.aliminderv` (vault)

#### 9.3.2 Encryption & Security

**User-Generated Passphrase**:
- Minimum 8 characters (enforced)
- **Passphrase Purpose**: Derives AES-256 encryption key using PBKDF2
- **Salt**: Randomly generated per export (included in payload header)
- **Iterations**: 100,000 PBKDF2 iterations (balance of security and speed)

**Encryption Scheme**:
```
AES-256-GCM (Galois/Counter Mode)
- Key: Derived from user passphrase via PBKDF2
- IV: Randomly generated (96-bit)
- Authentication Tag: 128-bit for integrity verification
```

**Checksum Verification**:
- **SHA-256 hash** of decrypted payload
- Displayed to user as first 8 characters for manual verification
- Example: "Checksum: A7F3D2E1"

#### 9.3.3 The QR-Code Handshake

**Method 1: High-Density QR Sequence**

**Old Device (Export)**:
1. User initiates "Migrate to New Device" from Settings
2. App encrypts payload using user-provided passphrase
3. Payload split into **QR Code Sequence** (max 2953 bytes per QR using QR v40)
4. Display QR codes in slideshow (auto-advance every 3 seconds)
5. User scans each QR code with new device sequentially

**QR Code Structure**:
```
QR Code 1: [Header: Total chunks, checksum] + [Chunk 1 data]
QR Code 2: [Chunk 2 data]
QR Code n: [Chunk n data]
```

**New Device (Import)**:
1. User selects "Scan Migration QR Code"
2. Camera scans each QR in sequence
3. Progress bar shows: "2 of 5 codes scanned"
4. After all codes scanned, prompts for passphrase
5. Decrypts, verifies checksum, imports data
6. Shows success confirmation + checksum for verification

**Method 2: Local Ad-Hoc Server (Fallback for Large Payloads)**

**Old Device**:
1. Creates local HTTP server on port 8080
2. Generates single QR code containing: `http://192.168.x.x:8080/migrate`
3. Displays WiFi network name and passphrase requirement

**New Device**:
1. Scans QR code
2. Connects to same WiFi network
3. Downloads encrypted payload via HTTP
4. Prompts for passphrase and decrypts

**Security**:
- Server only active while migration screen is open
- Payload remains encrypted during transfer
- Connection times out after 5 minutes

#### 9.3.4 File Export/Import (Manual Fallback)

**Export**:
- **Settings** → **Migrate Device** → **Export to File**
- Prompts for passphrase
- Saves encrypted `.aliminder` file to Downloads folder
- User can transfer via:
  - **Nearby Share** (Android to Android)
  - **AirDrop** (iOS to iOS, future)
  - **Email** (encrypted file is safe to share)
  - **USB transfer**
  - **Cloud storage** (user's choice, file is encrypted)

**Import**:
- **Onboarding** or **Settings** → **Import from File**
- File picker to select `.aliminder` file
- Prompts for passphrase
- Decrypts and imports data

#### 9.3.5 MigrationManager (Data Layer Component)

**Responsibilities**:

1. **Serialization**:
   - Export Room database tables to Protocol Buffers
   - Export SharedPreferences to structured format
   - Bundle custom audio assets (if any)

2. **Encryption**:
   - Generate random salt and IV
   - Derive key from passphrase using PBKDF2
   - Encrypt payload using AES-256-GCM
   - Compute SHA-256 checksum

3. **QR Generation**:
   - Split encrypted payload into QR-compatible chunks
   - Generate QR codes using ZXing library
   - Create header chunk with metadata

4. **QR Scanning**:
   - Decode QR codes using ML Kit or ZXing
   - Reassemble chunks in correct order
   - Handle missing/corrupt chunks

5. **Decryption**:
   - Prompt for passphrase
   - Derive key and attempt decryption
   - Verify checksum
   - Handle invalid passphrase (3 retry limit)

6. **Import**:
   - Clear existing local database (with user confirmation)
   - Insert Shadow Calendar entries
   - Insert Repercussion Library
   - Restore Sin Group configurations
   - Restore preferences

**Implementation Location**:
```
com.aliminder/
├── data/
│   ├── migration/
│   │   ├── MigrationManager.kt
│   │   ├── PayloadSerializer.kt
│   │   ├── QRCodeGenerator.kt
│   │   ├── QRCodeScanner.kt
│   │   ├── EncryptionManager.kt
│   │   └── LocalServerManager.kt (ad-hoc HTTP server)
```

#### 9.3.6 User Experience Flow

**Export Flow**:
```
Settings → Migrate Device → Choose Method
  ├── QR Code Transfer → Set Passphrase → Display QR Sequence
  ├── WiFi Transfer → Set Pass → Start Server → Show QR + Instructions
  └── Export File → Set Passphrase → Save to Downloads
```

**Import Flow**:
```
Onboarding/Settings → Import Data → Choose Method
  ├── Scan QR Code → Scan All → Enter Passphrase → Verify → Import
  ├── WiFi Transfer → Scan QR → Connect → Enter Pass → Import
  └── Import File → Select File → Enter Passphrase → Import
```

**Verification Screen** (Post-Import):
```
┌─────────────────────────────────────┐
│ ✅ Migration Successful             │
│                                     │
│ Checksum: A7F3D2E1 ✓                │
│                                     │
│ Imported:                           │
│ • 47 Shadow Calendar events         │
│ • 12 Hardwired Fears                │
│ • 8 Sin Group apps                  │
│ • All preferences restored          │
│                                     │
│ ⚠️ You must re-authenticate:        │
│ • Microsoft 365                     │
│ • Google Workspace                  │
│                                     │
│ [Continue to App]                   │
└─────────────────────────────────────┘
```

#### 9.3.7 Technical Requirements

**Dependencies**:
- **ZXing** (Zebra Crossing): QR code generation and scanning
- **Protocol Buffers**: Efficient serialization
- **Ktor** or **NanoHTTPD**: Local ad-hoc server (optional)
- **AndroidX Security Crypto**: Key derivation (PBKDF2)

**Permissions**:
- `CAMERA` (for QR scanning)
- `WRITE_EXTERNAL_STORAGE` (API < 29, for file export)

**Cross-Platform Compatibility**:
- Protocol Buffer schema must be identical on Android and iOS
- Encryption parameters must match exactly
- Checksum algorithm must be consistent

---

## 10. REDEMPTION & PRAISE SYSTEM

### 10.1 Victory Trigger Detection

**Positive Behavior Signals**:
1. **Accelerometer Surge**: Sudden upward movement (standing up)
2. **Sin Group Exit**: Closing the target app
3. **Screen Lock**: Device locked during intervention
4. **Physical Movement**: Sustained movement > 0.2 m/s² for 10s

### 10.2 The Victory Interrupt

**If victory occurs DURING a nag**:

1. **Record Scratch**: Play sharp `scratch.wav` (200-300ms)
2. **Immediate Cutoff**: Stop current Mad-Lib mid-sentence
3. **Grudging Validation**: Play audio-only praise clip
4. **No Text Notification**: Audio feedback only

**Example Clips**:
- *"Oh, look at you, being an adult."*
- *"Well. That's... unexpected. Good job, I guess."*
- *"Finally. Was beginning to think you forgot how to stand."*

### 10.3 Standalone Praise

**If user leaves Sin Group without active nag**:
- Brief positive acknowledgment
- Softer needle drop (celebratory variant)
- No sarcasm in Optimistic stage
- Mild approval in Weary stage

---

## 11. TECHNICAL ARCHITECTURE & CONSTRAINTS

### 11.1 Platform Requirements

**Target Platforms**:
- **Android**: v8.0 (API 26) and above
- **iOS**: v14.0 and above

**Development Approach**:
- **Native Implementations**: Kotlin (Android) + Swift (iOS)
- ***OR* Cross-Platform**: React Native / Flutter (if TTS and sensor APIs are robust)

### 11.2 System Permissions

**Android**:
- `USAGE_STATS` (foreground app detection)
- `BODY_SENSORS` (accelerometer, if not default)
- `ACCESS_FINE_LOCATION` (geofencing)
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (**mandatory**)
- `READ_CALENDAR`, `WRITE_CALENDAR`
- `USE_BIOMETRIC`

**iOS**:
- `DeviceActivity` framework (app usage)
- `Motion & Fitness` (accelerometer)
- `Location When In Use`
- `Calendars` access
- `Face ID / Touch ID`

### 11.3 Zero-Backend Architecture

**Constraints**:
- ❌ No cloud storage
- ❌ No remote accounts or databases
- ❌ No server-side processing
- ✅ All logic runs on-device
- ✅ All TTS is native (Android/iOS engines)
- ✅ All storage is local encrypted database

**Authentication**:
- **Biometric-Only**: Face ID / Touch ID / Fingerprint
- No email/password combinations
- No account recovery (user responsibility to backup)

### 11.4 Battery & Performance Optimization

**No Persistent Background Service**:
- Use **scheduled alarms** instead of continuous monitoring
- Android: `setAlarmClock()` API
- iOS: `PushKit` or `BGTaskScheduler` (BackgroundTasks framework)

**TTS Engine Management**:
- Initialize **only during** 10-second prep window
- `shutdown()` immediately after stitch completion
- Prevents RAM/battery bleed from idle TTS processes

**Monitoring Intervals**:
- **Active Sin Group**: Check every 30s
- **Normal Operation**: Check every 5m
- **Screen Off**: Suspend until screen-on event

---

## 12. DEVELOPER TEST HARNESS

> [!TIP]
> A **non-user-facing** debug layer for wireless on-device testing.

### 12.1 Hiss-Bench: Real-Time Audio Tuning

**Adjustable Parameters**:
- `jank_delay_ms` (gap between Mad-Lib components)
- `noise_floor_volume_db` (white noise loudness)
- `needle_drop_intensity` (thud impact)
- `wow_flutter_amount` (pitch modulation depth)

**Interface**:
- Wireless control via ADB (Android) or Xcode console (iOS)
- Live updates without app restart
- Visual waveform analyzer (optional)

### 12.2 Force Trigger Commands

**Android (ADB)**:
```bash
adb shell am broadcast -a com.aliminder.TEST \
  --es app "Reddit" \
  --ei urgency 3 \
  --es fear "boss_disapproval"
```

**iOS (Xcode Console)**:
```swift
AlimMinderDebug.forceIntervention(
  app: "TikTok",
  stage: .grave,
  fear: "missed_promotion"
)
```

### 12.3 Async Audit Logging

**Self-Audit Metrics**:
- `TTS_Ready_Timestamp` vs `Needle_Drop_Timestamp`
- `Target_Gap_ms` vs `Actual_Gap_ms`
- `Cold_Start_Lag_ms` (TTS initialization time)
- `Audio_Ducking_Success` (boolean)

**Log Format** (JSON):
```json
{
  "timestamp": "2025-12-28T05:20:00Z",
  "intervention_id": "uuid",
  "stage": "weary",
  "sin_group_app": "Instagram",
  "tts_ready_ms": 8734,
  "needle_drop_ms": 9000,
  "target_gap_ms": 250,
  "actual_gap_ms": 263,
  "audio_ducking": true,
  "wow_flutter_applied": false
}
```

---

## 13. COMPREHENSIVE ASSET LIBRARY

### 13.1 Audio Asset Taxonomy

| Asset Type | Count | Format | Purpose |
|------------|-------|--------|---------|
| **Human Intro (Optimistic)** | 10-15 | WAV, 44.1kHz, 16-bit | Stage 1 opening clips |
| **Human Intro (Weary)** | 10-15 | WAV, 44.1kHz, 16-bit | Stage 2 opening clips |
| **Human Intro (Grave)** | 10-15 | WAV, 44.1kHz, 16-bit | Stage 3 opening clips |
| **Human Outro (Optimistic)** | 10-15 | WAV, 44.1kHz, 16-bit | Stage 1 closing clips |
| **Human Outro (Weary)** | 10-15 | WAV, 44.1kHz, 16-bit | Stage 2 closing clips |
| **Human Outro (Grave)** | 10-15 | WAV, 44.1kHz, 16-bit | Stage 3 closing clips (fear-specific) |
| **Needle Drops** | 5+ | WAV, 44.1kHz, 16-bit | Randomized thud + pop intros |
| **Record Groove Loop** | 1 | WAV, 44.1kHz, 16-bit, seamless | White noise floor |
| **Record Scratch** | 1 | WAV, 44.1kHz, 16-bit | Victory interrupt |
| **Needle Lift Clunk** | 1 | WAV, 44.1kHz, 16-bit | Mechanical outro |
| **Grudging Praise** | 10+ | WAV, 44.1kHz, 16-bit | Victory validation clips |

**Total Asset Count**: **90-100 audio files**

### 13.2 Asset Organization

```
/assets/audio/
├── intros/
│   ├── optimistic/
│   │   ├── opt_intro_001.wav
│   │   ├── opt_intro_002.wav
│   │   └── ...
│   ├── weary/
│   └── grave/
├── outros/
│   ├── optimistic/
│   ├── weary/
│   └── grave/
│       ├── fear_boss_001.wav
│       ├── fear_social_001.wav
│       └── ...
├── mechanicals/
│   ├── needle_drop_001.wav
│   ├── needle_drop_002.wav
│   ├── record_scratch.wav
│   ├── needle_lift.wav
│   └── record_groove_loop.wav
└── praise/
    ├── grudging_001.wav
    ├── grudging_002.wav
    └── ...
```

### 13.3 Asset Production Guidelines

**Recording Quality**:
- Sample Rate: **44.1 kHz** (CD quality)
- Bit Depth: **16-bit** (balance of quality and file size)
- Format: **WAV** (uncompressed for processing)
- Normalize to: **-3dB peak** (headroom for processing)

**Voice Recording**:
- Neutral American accent (or user-preferred)
- Natural pacing with intentional breaths/pauses
- Multiple takes per script for variety
- Slight room tone (not studio-perfect) for authenticity

**Mechanical Sounds**:
- Authentic vinyl records preferred
- High-quality field recordings
- Minimal post-processing (preserve character)

---

## 14. USER INTERFACE ARCHITECTURE & NAVIGATION

> [!IMPORTANT]
> **ADHD-Optimized Design**: The interface prioritizes clarity and reduces choice paralysis through urgency-based sorting and contextual filtering.

### 14.1 The "Unified Sentinel" (Default ALL Page)

**Purpose**: The "Command Center" aggregating all data points from multiple sources into a single, urgency-sorted view.

**Data Sources**:
- Microsoft 365 calendar events
- Google Workspace calendar events
- AliMinder Shadow Calendar reminders
- Sin Group activity status

**The Urgency Sort**:
- Items are **NOT** sorted by "Time Created" or "Start Time"
- Items are sorted by **Proximity to PoNR** (ascending)
- Most urgent items (closest to PoNR) appear at the top
- Past PoNR items (Grave stage) highlighted in critical section

**Visual Indicators**:

| Element | Purpose | Behavior |
|---------|---------|----------|
| **Status Ring** | Persona stage indicator | Green (Optimistic), Orange (Weary), Red (Grave) |
| **Delta Display** | Time until PoNR | "+45m" (safe), "−10m" (late), dynamic color |
| **Provider Icon** | Data source badge | M365 logo, Google logo, or AliMinder icon |
| **Sin Group Alert** | Active distraction warning | Pulsing icon if currently in Sin Group app |

**Card Layout** (per event):
```
┌─────────────────────────────────────┐
│ [●] 9:00 AM - Staff Meeting    [-15m] │ ← Status Ring + Delta
│ Microsoft 365 • Conference Room A   │ ← Provider + Location
│ PoNR: 8:30 AM (Leave by 8:15 AM)   │ ← Calculated PoNR
│ 🚨 Currently on Reddit             │ ← Sin Group warning (if active)
└─────────────────────────────────────┘
```

**Tap Behavior**:
- Single tap → Opens **PoNR Math Drill-Down View** (see 14.5)
- Long press → Quick actions (snooze, dismiss, edit)

### 14.2 Specialized Filtering (Contextual Pages)

Beyond the "All" view, dedicated sub-pages allow focused "Corporate Vigilance."

#### 14.2.1 Category Filters

**Work Only**:
- Shows only Microsoft 365 events + work-tagged Shadow reminders
- Filters by calendar type or location (office geofence)
- Useful during work hours for focused vigilance

**Home/Personal**:
- Google Calendar personal events
- Shadow reminders tagged "personal"
- Grooming routines, errands, appointments

**Custom Categories**:
- User-defined tags (e.g., "Fitness," "Family," "Side Projects")
- Applied to Shadow Calendar entries
- Quick-filter chips at top of ALL page

#### 14.2.2 The "Sin Group" Filter

**Purpose**: Dedicated view showing apps AliMinder is actively hunting.

**Display**:
- List of all Sin Group apps (configured by user)
- **Today's Activity**: Time spent in each app during critical windows
- **Intervention Count**: How many nags were triggered per app
- **Victory Rate**: % of times user exited app after nag

**Example**:
```
┌─────────────────────────────────────┐
│ 📱 Instagram                         │
│ Energy Level: High                  │
│ Grace Period: 60s                   │
│ Today: 23m during critical windows  │
│ Interventions: 5 | Victories: 3     │
└─────────────────────────────────────┘
```

**Actions**:
- Tap app → Edit energy level, grace period
- Toggle to temporarily disable monitoring
- View full activity history

#### 14.2.3 The "Repercussion" Page

**Purpose**: Manage the "Hardwired Fears" library used in Grave stage scripting.

**Features**:
- **Category Tabs**: Professional / Social / Financial / Personal
- **Fear Cards**: Each fear displays:
  - Custom user text (e.g., "Boss's disapproval")
  - Gravity Score slider (1-10)
  - Context tags (work, social, etc.)
  - Associated audio outro clips count
- **Preview Button**: Play sample Grave stage intervention using this fear

**Association Logic**:
- User can link specific fears to specific calendar categories
- Example: "Missed promotion" → Microsoft 365 1:1 meetings
- Example: "Late fees" → Personal finance appointments

#### 14.2.4 The "Provider View"

**Purpose**: Separate tabs to audit the health of calendar syncs.

**Microsoft 365 Tab**:
- OAuth connection status
- Last sync timestamp
- Token expiry countdown
- Upcoming events count (next 7 days)
- **Re-sync Now** button
- **Re-authenticate** button

**Google Workspace Tab**:
- Same structure as M365 tab
- Independent sync status
- Separate OAuth token management

**Indicators**:
- 🟢 Green: Connected, token valid, recent sync
- 🟡 Yellow: Token expiring soon (< 7 days)
- 🔴 Red: Disconnected or token expired

### 14.3 Navigation: The "No-Nonsense" UI

**Design Principle**: Minimize choice paralysis for ADHD users.

**Primary Navigation** (Bottom Bar):
```
┌─────────────────────────────────────┐
│                                     │
│         [Main Content Area]         │
│                                     │
└─────────────────────────────────────┘
│  [ALL]  [Filters]  [+]  [Settings]  │ ← Bottom nav (4 items max)
└─────────────────────────────────────┘
```

**Tab Descriptions**:
1. **ALL**: Unified Sentinel dashboard (default landing)
2. **Filters**: Quick access to category toggles and Sin Group view
3. **+ (Add)**: Create new Shadow Calendar reminder
4. **Settings**: App configuration, provider sync, fears editor

**Filter Sheet** (swipe-up modal from Filters tab):
- **Chips**: Work | Personal | Custom Tags
- **Sin Group** button → Opens Sin Group Filter view
- **Repercussions** button → Opens Repercussion Page
- **Providers** button → Opens Provider View

**Interaction Patterns**:
- **Swipe-to-refresh**: Re-sync all providers on ALL page
- **Pull-down**: Reveal current Sin Group activity status
- **No hamburger menus**: All navigation visible and labeled
- **No nested sub-menus**: Max 2 taps to any feature

### 14.4 The "PoNR Math" Drill-Down View

**Trigger**: Tap any event on the ALL dashboard.

**Purpose**: Show exactly how PoNR is calculated, building user trust and awareness.

**Layout**:
```
┌─────────────────────────────────────┐
│ Staff Meeting - 9:00 AM             │
│ Delta: +45 minutes (Optimistic)     │
├─────────────────────────────────────┤
│ PoNR CALCULATION                    │
│                                     │
│ Event Start Time:       9:00 AM     │
│ − Commute Time:         15 min      │
│ − Prep/Grooming:        10 min      │
│ − Buffer:                5 min      │
│ ─────────────────────────────       │
│ = PoNR (Leave by):      8:30 AM     │
│                                     │
│ Current Time:           7:45 AM     │
│ Time Until PoNR:       +45 min      │
├─────────────────────────────────────┤
│ [Edit Commute] [Edit Prep] [Edit Buffer] │
│ [Snooze 10m] [Dismiss] [Add to Shadow]  │
└─────────────────────────────────────┘
```

**Editable Fields**:
- **Commute Time**: Tap to adjust (increments of 5 min)
- **Prep/Grooming**: Tap to adjust (increments of 5 min)
- **Buffer**: Tap to adjust (increments of 5 min)
- Changes apply to this event only (or "Save as default" option)

**Real-Time Updates**:
- Delta recalculates immediately as user adjusts values
- Status Ring color updates if stage threshold is crossed
- Preview of when intervention would trigger

**Actions**:
- **Snooze 10m**: Temporarily suppress interventions for this event
- **Dismiss**: Remove from urgency tracking (event stays in calendar)
- **Add to Shadow**: Create a custom AliMinder-only reminder for this event

### 14.5 ADHD-Friendly Design Patterns

**Visual Hierarchy**:
- **High Contrast**: Status rings and delta displays use vibrant colors
- **Large Touch Targets**: Minimum 48dp tap areas
- **Generous Spacing**: Avoid cramped interfaces that require precision

**Information Density**:
- **Progressive Disclosure**: Show essentials on card, details on tap
- **No Wall of Text**: Maximum 2 lines per card on ALL view
- **Visual Anchors**: Consistent icon placement, color coding

**Interaction Feedback**:
- **Immediate Response**: All taps show instant visual feedback
- **Haptic Feedback**: Subtle vibration on critical actions
- **Audio Confirmation**: Optional "click" sound on saves/edits

**Animation Constraints**:
- **No Gratuitous Motion**: Animations serve functional purpose only
- **Respect Reduced Motion**: Honor system accessibility settings
- **Quick Transitions**: Max 200ms for view changes

---

## 15. USER CONFIGURATION & ONBOARDING

### 15.1 Onboarding Flow

1. **Welcome & Philosophy**: Explain AliMinder's persona and approach
2. **Permissions Request**: Sequential permission requests with rationale
3. **Biometric Setup**: Configure Face ID / Fingerprint
4. **Calendar Sync**: OAuth flow for M365/Google
5. **Sin Group Configuration**: Select distraction apps
6. **Hardwired Fears**: Define personal repercussions (3-5 minimum)
7. **PoNR Configuration**: Set default commute/prep times
8. **Audio Test**: Preview needle drop and Mad-Lib stitch

### 15.2 Additional Configuration Screens

**Dashboard**:
- Next Event with PoNR countdown
- Current Delta status (visual progress bar)
- Sin Group activity summary (today's distractions)
- Quick access to Shadow Calendar

**Sin Group Manager**:
- Installed apps list with checkbox selection
- Energy level assignment (High/Medium/Low)
- Per-app grace period customization

**Hardwired Fears Editor**:
- Fear category selection (Professional/Social/Financial/Personal)
- Custom fear text entry
- Gravity score slider (1-10)
- Context tag assignment
- Preview associated outro clips

**Settings**:
- Audio customization (volume, alarm stream, silence mode behavior)
- Kill-switch configuration (enable/disable flip-to-mute)
- Default PoNR parameters (commute, prep, buffer)
- Developer harness access (hidden, requires 7 taps)

---

## 16. PRIVACY & SECURITY

### 16.1 Data Storage

**Local Encrypted Database**:
- **Engine**: SQLite with SQLCipher (AES-256 encryption)
- **Key Management**: Derived from biometric authentication
- **Location**: App-specific private directory

**Stored Data**:
- Calendar events (cached for 7 days)
- Sin Group configurations
- Hardwired Fears library
- Shadow Calendar entries
- Intervention logs (anonymized)
- OAuth refresh tokens (encrypted separately)

### 16.2 Privacy Policy Compliance

**Data Collection**: NONE
- No analytics
- No crash reporting (unless user explicitly enables local-only debug logs)
- No third-party SDKs beyond OAuth libraries

**Third-Party Communication**:
- **Only**: Microsoft Graph API and Google Calendar API
- **Frequency**: On-demand for calendar sync
- **Data Shared**: OAuth tokens only (standard protocol)

**User Rights**:
- Full data export (Personality Bundle)
- Immediate data deletion (uninstall = complete removal)
- No account recovery (user responsibility)

---

## 17. RISK ANALYSIS & MITIGATION

### 17.1 Technical Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| **TTS Cold Start Lag** | High | High | Pre-initialize at T+0s, 9s buffer, fallback to audio-only |
| **Battery Drain** | Medium | High | Alarm-based scheduling, immediate TTS shutdown, optimization testing |
| **Permission Denial** | High | Critical | Graceful degradation, clear rationale, progressive permissions |
| **Audio Focus Conflicts** | Medium | Medium | High-priority alarm stream, ducking instead of pause |
| **OAuth Token Expiry** | Low | Medium | Automatic refresh, user notification if manual re-auth needed |

### 17.2 User Experience Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| **Habituation** | High | Critical | Anti-habituation randomization, persona evolution, wow/flutter |
| **Annoyance** | Medium | High | Kill-switch, user control over aggression, redemption system |
| **False Positives** | Medium | Medium | Grace periods, context awareness, manual snooze option |
| **Accessibility Issues** | Low | Medium | High contrast UI, screen reader support, volume normalization |

---

## 18. IMPLEMENTATION ROADMAP

### Phase 1: Foundation (Weeks 1-3)
- [ ] Project setup (Android/iOS or cross-platform decision)
- [ ] Local encrypted storage implementation
- [ ] Biometric authentication
- [ ] Basic UI framework

### Phase 2: Calendar Integration (Weeks 4-6)
- [ ] OAuth2 with PKCE implementation
- [ ] M365 Graph API integration
- [ ] Google Calendar API integration
- [ ] Shadow Calendar CRUD operations
- [ ] PoNR calculation engine

### Phase 3: Monitoring Systems (Weeks 7-9)
- [ ] USAGE_STATS / DeviceActivity implementation
- [ ] Accelerometer monitoring
- [ ] Geofencing and Wi-Fi detection
- [ ] Digital Stasis detection logic

### Phase 4: Audio Engine (Weeks 10-14)
- [ ] 10-second prep window timing system
- [ ] Audio ducking implementation
- [ ] Mad-Lib stitching engine
- [ ] White noise floor integration
- [ ] Wow and Flutter effects
- [ ] Asset integration and management

### Phase 5: Dynamic Scripting (Weeks 15-17)
- [ ] Social Mirror selection logic
- [ ] Hardwired Fears configuration UI
- [ ] Contextual matching algorithms
- [ ] Persona state machine

### Phase 6: Kill-Switch & Redemption (Weeks 18-19)
- [ ] Accelerometer flip detection
- [ ] Proximity sensor integration
- [ ] Victory trigger detection
- [ ] Praise system implementation

### Phase 7: Polish & Testing (Weeks 20-24)
- [ ] Developer test harness
- [ ] Multi-device testing
- [ ] Battery optimization verification
- [ ] Asset production and integration
- [ ] User testing and feedback iteration

### Phase 8: Deployment (Weeks 25-26)
- [ ] App Store submission preparation
- [ ] Play Store submission preparation
- [ ] Documentation and support materials
- [ ] Beta release

**Total Estimated Timeline**: **6 months** (26 weeks)

---

## 19. SUCCESS METRICS

### 19.1 Technical Metrics

- **TTS Cold Start**: < 9s on 95% of devices
- **Audio Gap Precision**: Target ±50ms variance
- **Battery Impact**: < 5% per 8-hour workday
- **Crash Rate**: < 0.1% of sessions

### 19.2 User Behavior Metrics (Local Analytics Only)

- **Intervention Success Rate**: Distraction stopped within 2 minutes
- **Victory Events**: Positive behavior changes per day
- **Kill-Switch Usage**: Frequency of panic mute (indicates false positives)
- **Sin Group Modification**: User refinement of distraction list

### 19.3 User Satisfaction (Qualitative)

- **Persona Effectiveness**: Does the tone resonate?
- **Audio Aesthetic**: Does the "jank" feel intentional and effective?
- **Habituation**: Are users still responding after 30 days?

---

## 20. APPENDICES

### Appendix A: Technical Glossary

- **PoNR**: Point of No Return (latest moment to depart and arrive on time)
- **Mad-Lib**: Audio stitching technique combining pre-recorded and TTS
- **Sin Group**: User-defined list of distraction apps
- **Digital Stasis**: Detected state of prolonged, motionless app engagement
- **Aesthetic Friction**: Intentional audio imperfection for attention capture
- **Wow and Flutter**: Mechanical playback degradation effects

### Appendix B: Recommended Third-Party Libraries

**Audio Processing**:
- Android: `ExoPlayer`, `AudioTrack` (native)
- iOS: `AVFoundation`, `AVAudioEngine`

**OAuth**:
- `AppAuth` (Android/iOS) for PKCE implementation

**Encryption**:
- `SQLCipher` (cross-platform encrypted SQLite)

**Sensors**:
- Native Android `SensorManager`
- Native iOS `CoreMotion`

### Appendix C: Example Mad-Lib Scripts

**Optimistic Stage**:
- Intro: "Hey there, I see you're on..."
- TTS: "[Reddit]"
- Outro: "...but we've got that [Client Meeting] in 30 minutes. Just a heads-up."

**Weary Stage**:
- Intro: "*sigh* Still scrolling through..."
- TTS: "[Instagram]"
- Outro: "...fantastic. The [Standup] is in 10 minutes. I'm exhausted for you."

**Grave Stage** (Boss Disapproval Fear):
- Intro: "It's too late. You're officially late to..."
- TTS: "[Board Presentation]"
- Outro: "...and right now, your boss is wondering where you are. I hope [Twitter] was worth your career."

---

## 21. CONCLUSION

AliMinder represents a novel approach to assistive technology for neurodivergent users: **a character-driven, aesthetically intentional, zero-backend vigilance system**. By combining dynamic persona evolution, contextual audio scripting, mechanical aesthetic friction, and intelligent distraction detection, AliMinder creates an experience that is:

- **Emotionally Resonant**: The Witty-to-Weary arc mirrors the user's descent
- **Attention-Grabbing**: Aesthetic friction breaks through digital hypnosis
- **Privacy-Preserving**: Zero-backend architecture ensures complete data ownership
- **Context-Aware**: Social Mirror and PoNR algorithms adapt to user's specific fears and schedules

This specification provides the complete blueprint for implementation. All architectural decisions, technical constraints, and user experience mechanisms are now defined and ready for development execution.

---

**Document Version**: 1.0  
**Last Updated**: 2025-12-28  
**Status**: Ready for Implementation  
**Rigor Protocol**: Audited and Approved
