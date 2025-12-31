# AliMinder Android Architecture
## Technical Implementation Specification

> **Status**: Phase 1 - Foundation  
> **Platform**: Native Android (Kotlin)  
> **Architecture**: MVVM + Clean Architecture  
> **UI Framework**: Jetpack Compose

---

## 1. ARCHITECTURAL DECISIONS

### 1.1 Technology Stack Confirmation

| Component | Technology | Rationale |
|-----------|------------|-----------|
| **Language** | Kotlin | Modern, null-safe, coroutines support |
| **UI Framework** | Jetpack Compose | Reactive UI for persona state changes, urgency sorting |
| **Architecture** | MVVM + Clean | Separation of concerns, testability |
| **DI** | Hilt | Google-recommended, built on Dagger |
| **Database** | Room + SQLCipher | Local encrypted storage, type-safe queries |
| **OAuth** | AppAuth | PKCE support, industry standard |
| **Async** | Kotlin Coroutines | Structured concurrency |
| **Audio (Voice/Loops)** | Media3/ExoPlayer | Precise playback control, format support |
| **Audio (Transients)** | SoundPool | Zero-latency needle thuds/pops |
| **QR Codes** | ZXing | QR generation and scanning |
| **Serialization** | Protocol Buffers | Cross-platform migration compatibility |

### 1.2 SDK Configuration

```kotlin
// build.gradle.kts (app module)
android {
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.aliminder.app"
        minSdk = 26  // Android 8.0 (Oreo)
        targetSdk = 34  // Android 14
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

---

## 2. PROJECT STRUCTURE (Clean Architecture Layers)

```
com.aliminder.app/
├── 📂 data/                          # Data Layer
│   ├── local/                        # Local data sources
│   │   ├── database/
│   │   │   ├── AppDatabase.kt        # Room database with SQLCipher
│   │   │   ├── dao/
│   │   │   │   ├── ShadowEventDao.kt
│   │   │   │   ├── RepercussionDao.kt
│   │   │   │   ├── SinGroupDao.kt
│   │   │   │   └── InterventionLogDao.kt
│   │   │   └── entities/
│   │   │       ├── ShadowEventEntity.kt
│   │   │       ├── RepercussionEntity.kt
│   │   │       └── SinGroupAppEntity.kt
│   │   └── preferences/
│   │       └── PreferencesManager.kt  # DataStore preferences
│   ├── remote/                       # Remote data sources
│   │   ├── microsoft/
│   │   │   └── M365CalendarApi.kt
│   │   └── google/
│   │       └── GoogleCalendarApi.kt
│   ├── audio/                        # Audio Engine (Data Layer)
│   │   ├── VinylStackEngine.kt       # **Standalone Audio Service**
│   │   ├── AudioScheduler.kt         # 10-second prep window timing
│   │   ├── MadLibStitcher.kt         # Intro + TTS + Outro assembly
│   │   ├── NeedleDropPlayer.kt       # SoundPool for transients
│   │   ├── VoiceClipPlayer.kt        # Media3 for voice/loops
│   │   ├── AudioDucker.kt            # Media volume control
│   │   ├── WowFlutterProcessor.kt    # Pitch/speed modulation
│   │   └── AudioAssetManager.kt      # Asset selection logic
│   ├── sensors/                      # Sensor monitoring
│   │   ├── AccelerometerMonitor.kt   # Flip detection, movement
│   │   ├── UsageStatsMonitor.kt      # Foreground app detection
│   │   └── LocationMonitor.kt        # Geofence, WiFi SSID
│   ├── migration/                    # **QR-Handshake Protocol**
│   │   ├── MigrationManager.kt
│   │   ├── PayloadSerializer.kt
│   │   ├── EncryptionManager.kt
│   │   ├── QRCodeGenerator.kt
│   │   ├── QRCodeScanner.kt
│   │   └── LocalServerManager.kt
│   └── repository/                   # Repository implementations
│       ├── CalendarRepository.kt     # **Unified Shadow + Provider Stream**
│       ├── AudioRepository.kt
│       ├── SinGroupRepository.kt
│       ├── RepercussionRepository.kt
│       └── MigrationRepository.kt
│
├── 📂 domain/                        # Domain Layer (Business Logic)
│   ├── model/                        # Domain models (pure Kotlin)
│   │   ├── Event.kt                  # Unified event (M365/Google/Shadow)
│   │   ├── PersonaStage.kt           # Optimistic/Weary/Grave
│   │   ├── PoNRCalculation.kt
│   │   ├── SinGroupApp.kt
│   │   ├── Repercussion.kt
│   │   └── InterventionTrigger.kt
│   ├── usecase/                      # Use Cases (business operations)
│   │   ├── ponr/
│   │   │   ├── CalculatePoNRUseCase.kt
│   │   │   ├── CalculateDeltaUseCase.kt
│   │   │   └── DeterminePersonaStageUseCase.kt
│   │   ├── intervention/
│   │   │   ├── TriggerInterventionUseCase.kt
│   │   │   ├── SelectAudioClipsUseCase.kt  # Social Mirror logic
│   │   │   └── ApplyWowFlutterUseCase.kt
│   │   ├── monitoring/
│   │   │   ├── DetectDigitalStasisUseCase.kt
│   │   │   ├── MonitorSinGroupUseCase.kt
│   │   │   └── DetectVictoryUseCase.kt
│   │   ├── calendar/
│   │   │   ├── SyncCalendarsUseCase.kt
│   │   │   ├── GetUpcomingEventsUseCase.kt  # Urgency-sorted
│   │   │   └── CreateShadowEventUseCase.kt
│   │   └── migration/
│   │       ├── ExportDataUseCase.kt
│   │       └── ImportDataUseCase.kt
│   └── repository/                   # Repository interfaces (contracts)
│       ├── ICalendarRepository.kt
│       ├── IAudioRepository.kt
│       └── IMigrationRepository.kt
│
└── 📂 presentation/                  # Presentation Layer (UI + ViewModels)
    ├── theme/                        # Jetpack Compose theme
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    ├── navigation/                   # Navigation graph
    │   └── AppNavigation.kt
    ├── components/                   # Reusable Compose components
    │   ├── EventCard.kt              # Status ring, delta display
    │   ├── StatusRing.kt             # Green/Orange/Red persona indicator
    │   └── PoNRMathCard.kt
    ├── screens/                      # App screens
    │   ├── all/                      # Unified Sentinel (ALL dashboard)
    │   │   ├── AllScreen.kt
    │   │   └── AllViewModel.kt
    │   ├── filters/
    │   │   ├── FiltersScreen.kt
    │   │   └── FiltersViewModel.kt
    │   ├── singroup/                 # Sin Group management
    │   │   ├── SinGroupScreen.kt
    │   │   └── SinGroupViewModel.kt
    │   ├── repercussions/            # Hardwired Fears editor
    │   │   ├── RepercussionsScreen.kt
    │   │   └── RepercussionsViewModel.kt
    │   ├── providers/                # Provider health audit
    │   │   ├── ProvidersScreen.kt
    │   │   └── ProvidersViewModel.kt
    │   ├── ponr_math/                # Drill-down calculation view
    │   │   ├── PoNRMathScreen.kt
    │   │   └── PoNRMathViewModel.kt
    │   ├── settings/
    │   │   ├── SettingsScreen.kt
    │   │   └── SettingsViewModel.kt
    │   ├── migration/                # QR-Code import/export
    │   │   ├── MigrationScreen.kt
    │   │   ├── QRScannerScreen.kt
    │   │   └── MigrationViewModel.kt
    │   └── onboarding/
    │       ├── OnboardingScreen.kt
    │       └── OnboardingViewModel.kt
    └── service/                      # Foreground service for monitoring
        └── VigilanceService.kt       # Always-on monitoring service
```

---

## 3. CRITICAL IMPLEMENTATION REQUIREMENTS

### 3.1 Audio Engine (Vinyl Stack) - Standalone Service

> **User Requirement**: Audio Engine must be in Domain/Data layers, handle its own async scheduling, independent of Presentation lifecycle.

**Implementation**:

```kotlin
// data/audio/VinylStackEngine.kt
class VinylStackEngine @Inject constructor(
    private val audioScheduler: AudioScheduler,
    private val madLibStitcher: MadLibStitcher,
    private val needleDropPlayer: NeedleDropPlayer,
    private val voiceClipPlayer: VoiceClipPlayer,
    private val audioDucker: AudioDucker,
    private val wowFlutterProcessor: WowFlutterProcessor,
    @AudioDispatcher private val dispatcher: CoroutineDispatcher  // Custom single-threaded
) {
    private val engineScope = CoroutineScope(dispatcher + SupervisorJob())
    
    /**
     * Initiates 10-second prep window with system-clock based scheduling.
     * Independent of UI lifecycle.
     */
    fun scheduleIntervention(trigger: InterventionTrigger) {
        engineScope.launch {
            // T+0s: Silent wake, TTS init begins
            val ttsInitJob = async { initializeTTS() }
            
            // T+9s: Audio ducking
            delay(9000)
            audioDucker.duckMediaVolume(targetVolume = 0.2f)
            
            // T+9.1s: Needle drop (randomized)
            delay(100)
            needleDropPlayer.playRandomNeedleDrop()
            
            // T+10s: Await TTS ready, then stitch
            ttsInitJob.await()
            val madLib = madLibStitcher.create(trigger)
            voiceClipPlayer.play(madLib)
            
            // Cleanup
            voiceClipPlayer.awaitCompletion()
            audioDucker.restoreMediaVolume()
            shutdownTTS()
        }
    }
}
```

**Coroutine Dispatcher Module**:

```kotlin
// di/DispatcherModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    @AudioDispatcher
    @Provides
    @Singleton
    fun provideAudioDispatcher(): CoroutineDispatcher {
        // Dedicated single-threaded dispatcher for audio timing precision
        return Executors.newSingleThreadExecutor { r ->
            Thread(r, "VinylStackThread").apply {
                priority = Thread.MAX_PRIORITY  // High priority for timing
            }
        }.asCoroutineDispatcher()
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AudioDispatcher
```

### 3.2 Repository Pattern - Unified Calendar Stream

> **User Requirement**: Repository must handle Shadow Layer + Provider data as unified stream.

```kotlin
// data/repository/CalendarRepository.kt
class CalendarRepository @Inject constructor(
    private val shadowEventDao: ShadowEventDao,
    private val m365CalendarApi: M365CalendarApi,
    private val googleCalendarApi: GoogleCalendarApi,
    private val ponrCalculator: CalculatePoNRUseCase
) : ICalendarRepository {
    
    /**
     * Returns unified stream of all events sorted by PoNR proximity.
     * Combines M365, Google, and Shadow calendars.
     */
    override fun getUpcomingEventsStream(): Flow<List<Event>> {
        return combine(
            shadowEventDao.getAllEventsFlow(),
            m365CalendarApi.getEventsFlow(),
            googleCalendarApi.getEventsFlow()
        ) { shadowEvents, m365Events, googleEvents ->
            
            // Merge all sources
            val allEvents = shadowEvents.map { it.toDomainModel() } +
                           m365Events +
                           googleEvents
            
            // Calculate PoNR for each event
            val eventsWithPoNR = allEvents.map { event ->
                val ponr = ponrCalculator.calculate(event)
                val delta = ponrCalculator.calculateDelta(ponr)
                event.copy(ponr = ponr, delta = delta)
            }
            
            // Sort by PoNR proximity (ascending delta)
            eventsWithPoNR.sortedBy { it.delta }
        }
    }
}
```

### 3.3 Audio Dependencies Configuration

```kotlin
// build.gradle.kts (app module)
dependencies {
    // Media3 (ExoPlayer) for voice clips and hiss loops
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    
    // SoundPool is part of Android SDK (android.media.SoundPool)
    // No additional dependency needed
    
    // Audio effects (for Wow & Flutter)
    implementation("androidx.media3:media3-effect:1.2.0")
}
```

**SoundPool for Transients**:

```kotlin
// data/audio/NeedleDropPlayer.kt
class NeedleDropPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)  // Max concurrent sounds
        .build()
    
    private val needleDropSounds: List<Int> = listOf(
        R.raw.needle_drop_001,
        R.raw.needle_drop_002,
        R.raw.needle_drop_003,
        R.raw.needle_drop_004,
        R.raw.needle_drop_005
    ).map { soundPool.load(context, it, 1) }
    
    fun playRandomNeedleDrop() {
        val soundId = needleDropSounds.random()
        soundPool.play(
            soundId,
            1.0f,  // Left volume
            1.0f,  // Right volume
            1,     // Priority
            0,     // Loop (0 = no loop)
            1.0f   // Rate (playback speed)
        )
    }
}
```

---

## 4. MIGRATION MANAGER ARCHITECTURE

### 4.1 Data Flow

```
Export:
User initiates → MigrationViewModel → ExportDataUseCase 
→ MigrationManager.export() → PayloadSerializer.serialize() 
→ EncryptionManager.encrypt() → QRCodeGenerator.generate()
→ UI displays QR codes

Import:
QR scanned → QRCodeScanner.decode() → PayloadSerializer.reassemble()
→ User enters passphrase → EncryptionManager.decrypt()
→ MigrationManager.import() → Database population → Done
```

### 4.2 Protocol Buffers Schema

```protobuf
// migration/migration.proto
syntax = "proto3";

package com.aliminder.migration;

message MigrationPayload {
    string version = 1;  // Schema version
    repeated ShadowEvent shadow_events = 2;
    repeated Repercussion repercussions = 3;
    repeated SinGroupApp sin_groups = 4;
    PersonalityConfig config = 5;
    repeated InterventionLog logs = 6;
}

message ShadowEvent {
    string id = 1;
    string title = 2;
    int64 start_time_millis = 3;
    int32 commute_minutes = 4;
    int32 prep_minutes = 5;
    int32 buffer_minutes = 6;
    string category = 7;
}

message Repercussion {
    string id = 1;
    string text = 2;
    int32 gravity_score = 3;
    string category = 4;  // professional, social, financial, personal
    repeated string context_tags = 5;
}

// ... etc
```

---

## 5. DEPENDENCY INJECTION (Hilt) SETUP

```kotlin
// di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        // SQLCipher encryption
        val passphrase = getDevicePassphrase(context)  // Derived from biometric
        val factory = SupportFactory(passphrase)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aliminder.db"
        )
        .openHelperFactory(factory)
        .build()
    }
    
    @Provides
    @Singleton
    fun provideVinylStackEngine(
        audioScheduler: AudioScheduler,
        madLibStitcher: MadLibStitcher,
        needleDropPlayer: NeedleDropPlayer,
        voiceClipPlayer: VoiceClipPlayer,
        audioDucker: AudioDucker,
        wowFlutterProcessor: WowFlutterProcessor,
        @AudioDispatcher dispatcher: CoroutineDispatcher
    ): VinylStackEngine {
        return VinylStackEngine(
            audioScheduler,
            madLibStitcher,
            needleDropPlayer,
            voiceClipPlayer,
            audioDucker,
            wowFlutterProcessor,
            dispatcher
        )
    }
}
```

---

## 6. PERMISSIONS & MANIFEST

```xml
<!-- AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Audio & Sensors -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    
    <!-- Calendar Access -->
    <uses-permission android:name="android.permission.READ_CALENDAR" />
    <uses-permission android:name="android.permission.WRITE_CALENDAR" />
    
    <!-- Usage Stats (Foreground App Detection) -->
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
    
    <!-- Location (Geofencing) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    
    <!-- Battery Optimization Bypass (CRITICAL) -->
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    
    <!-- Foreground Service -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    
    <!-- Camera (QR Scanning) -->
    <uses-permission android:name="android.permission.CAMERA" />
    
    <!-- Biometric -->
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
    
    <!-- Internet (OAuth only) -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application ...>
        
        <!-- Foreground Vigilance Service -->
        <service
            android:name=".presentation.service.VigilanceService"
            android:foregroundServiceType="location|microphone"
            android:exported="false" />
        
    </application>
</manifest>
```

---

## 7. IMPLEMENTATION PHASES

### Phase 1: Foundation (Current)
1. ✅ Project setup complete
2. ⏳ Configure Hilt DI
3. ⏳ Set up Room database + SQLCipher
4. ⏳ Create domain models
5. ⏳ Implement basic Compose UI theme

### Phase 2: Audio Engine Skeleton
1. Create VinylStackEngine stub
2. Implement AudioScheduler with system-clock timing
3. Integrate SoundPool for needle drops
4. Integrate Media3 for voice clips

### Phase 3: Calendar Integration
1. Implement Shadow Calendar (Room DAO)
2. Set up OAuth with AppAuth
3. Integrate M365 Graph API
4. Integrate Google Calendar API
5. Create unified CalendarRepository stream

### Phase 4: Migration System
1. Implement EncryptionManager (AES-256-GCM)
2. Create Protocol Buffers schema
3. Build QRCodeGenerator/Scanner
4. Create MigrationManager

### Phase 5: UI Implementation
1. Build ALL screen (Unified Sentinel)
2. Implement PoNR Math drill-down
3. Create Sin Group management
4. Build Repercussions editor

---

## 8. NEXT IMMEDIATE STEPS

Once you've completed the Android Studio setup:

1. **I'll generate the complete project structure** with all folders
2. **Configure build.gradle.kts** with all dependencies
3. **Set up Hilt** for dependency injection
4. **Create the Room database schema**
5. **Implement basic Compose navigation**
6. **Deploy to your Galaxy S22** to verify setup

**Are you ready to proceed with the Android Studio installation?**
