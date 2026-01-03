# AliMinder

A vigilance assistant Android app designed to help neurodivergent users (mild ADHD) prevent "time blindness" by calculating Point of No Return (PoNR) for calendar events and providing timely interventions.

## Overview

AliMinder calculates when you must leave to arrive on time, accounting for commute, preparation, and buffer time. The app monitors your attention and provides urgency-scaled notifications as deadlines approach.

**Key Concept: Point of No Return (PoNR)**
```
PoNR = Event Start Time - (Commute + Prep + Buffer)
```

## Tech Stack

### Core
- **Language**: Kotlin 1.9.23
- **Min SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture

### Dependencies
- **Dependency Injection**: Hilt 2.51.1
- **Database**: Room 2.6.1 + SQLCipher 4.5.4 (encrypted)
- **Async**: Kotlin Coroutines + Flow
- **Audio**: Media3/ExoPlayer 1.2.0
- **Navigation**: Jetpack Navigation Compose
- **Background Work**: WorkManager
- **OAuth**: AppAuth 0.11.1
- **QR Codes**: ZXing 3.5.2 + ML Kit Barcode Scanning
- **Encryption**: AndroidX Security Crypto
- **Calendar APIs**: Microsoft Graph 5.80.0, Google Calendar API
- **Migration**: Protocol Buffers 3.24.0

## Project Structure

```
app/src/main/
├── java/com/aliminder/app/
│   ├── domain/model/          # Domain models (Duty, PoNRCalculation, PersonaStage, etc.)
│   └── presentation/          # UI layer (MainActivity, theme)
└── kotlin/com/aliminder/app/
    ├── data/
    │   ├── audio/             # VinylStackEngine (audio playback)
    │   ├── local/             # Room database, DAOs, entities
    │   ├── mapper/            # Entity ↔ Domain mapping (PoNR calculations happen here)
    │   └── repository/        # Repository implementations
    ├── di/                    # Hilt modules
    ├── domain/
    │   ├── model/             # UserSettings
    │   ├── repository/        # Repository interfaces
    │   └── worker/            # Background workers
    └── presentation/
        ├── components/        # Reusable Compose components
        ├── navigation/        # Navigation setup
        └── screens/           # Feature screens (All, Dashboard, Settings, etc.)
```

## Setup

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17
- **Gradle**: 8.7.3 (via wrapper)

### Clone & Build
```bash
git clone <repository-url>
cd AliMinder
./gradlew build
```

### Run
1. Open project in Android Studio
2. Sync Gradle dependencies
3. Connect Android device or start emulator (API 26+)
4. Run app module

## Architecture

### PoNR Calculation Flow

1. **Data Layer** (`DutyMapper.toDomainDuty()`):
   - Gets duty from database with commute/prep/buffer settings
   - Calculates PoNR time: `startTime.minusMinutes(commute + prep + buffer)`
   - Computes delta: `Duration.between(now, ponrTime).toMinutes()`
   - Determines persona stage based on delta and urgency threshold

2. **Repository** (`DutyRepositoryImpl`):
   - Maintains reactive flow of all duties
   - Uses ticker flow to recalculate PoNR every minute (aligned to system clock)
   - Combines database updates + settings changes + time ticks

3. **Persona Stages** (4 stages):
   - **OPTIMISTIC**: Delta > urgency threshold (default 60 min)
   - **WEARY**: 0 ≤ Delta ≤ urgency threshold
   - **URGENT**: Delta < 0 (past PoNR, before event start)
   - **LATE**: Past event start time

### Database

**Encrypted Room Database** (`AliMinderDatabase`):
- **DutyEntity**: Events and tasks with PoNR parameters
- **UserSettingsEntity**: User preferences (urgency threshold, auto-hide settings)

**Encryption**: SQLCipher with passphrase-derived key

### Background Processing

- **WorkManager**: Periodic workers for auto-hiding overdue duties
- **Ticker Flow**: Real-time PoNR recalculation without battery-draining polling

## Current Features

✅ **Implemented**:
- PoNR calculation engine with 4 persona stages
- Room database with SQLCipher encryption
- Reactive flows for real-time duty updates
- User settings (urgency threshold, auto-hide timers)
- Duty dismissal system with reasons
- Auto-hide overdue duties worker
- Material 3 Compose UI with navigation
- Mock data for development

❌ **Not Yet Implemented**:
- Calendar sync (M365/Google)
- Audio intervention system
- Distraction detection
- QR-based device migration
- Geofencing
- OAuth integration

## Development

### Mock Data
See `MockData.kt` for sample events used in UI development.

### Database Initialization
`DatabaseInitializer.kt` seeds the database with sample duties on first launch.

### Testing
```bash
./gradlew test              # Unit tests
./gradlew connectedCheck    # Instrumentation tests
```

## Build Configuration

- **Proguard**: Enabled for release builds
- **Compose**: Enabled with Kotlin compiler extension 1.5.11
- **JNI**: Legacy packaging enabled for SQLCipher compatibility
