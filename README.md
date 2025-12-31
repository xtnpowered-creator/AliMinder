# AliMinder - Android Project

This directory contains the complete Android implementation of **AliMinder: The Vigilance Sentinel**.

## 📁 Project Structure

See [ARCHITECTURE.md](../ARCHITECTURE.md) for complete architectural details.

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 26-34
- Physical Android device (recommended) or emulator

### Setup Instructions
1. Follow [SETUP_GUIDE.md](../SETUP_GUIDE.md) to install Android Studio
2. Open this project in Android Studio
3. Wait for Gradle sync to complete
4. Connect your Galaxy S22 via USB
5. Click Run (▶) to build and install

## 📚 Documentation

- **[MASTER_SPECIFICATION.md](../MASTER_SPECIFICATION.md)**: Complete product specification
- **[ARCHITECTURE.md](../ARCHITECTURE.md)**: Technical architecture and implementation details
- **[SETUP_GUIDE.md](../SETUP_GUIDE.md)**: Beginner-friendly setup walkthrough

## 🏗️ Current Status

**Phase 1: Foundation** ✅ IN PROGRESS

### Completed
- ✅ Gradle build configuration with all dependencies
- ✅ Hilt dependency injection setup
- ✅ Material3 Compose theme (ADHD-optimized dark theme)
- ✅ Core domain models (Event, PersonaStage, PoNRCalculation, SinGroupApp, Repercussion)
- ✅ Custom @AudioDispatcher for VinylStackEngine

### Next Steps
- Room database schema
- Basic Compose navigation
- PoNR calculation use case
- Audio engine skeleton

## 🔧 Build Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## 📦 Key Dependencies

- **Jetpack Compose**: Modern declarative UI
- **Hilt**: Dependency injection
- **Room + SQLCipher**: Encrypted local database
- **Media3/ExoPlayer**: Audio playback (voice clips, loops)
- **AppAuth**: OAuth2 with PKCE
- **Protocol Buffers**: Cross-platform data serialization
- **ZXing**: QR code generation/scanning

## 🎯 Architecture Highlights

- **MVVM + Clean Architecture**: Separation of concerns
- **Standalone Audio Engine**: Dedicated high-priority thread for precise timing
- **Unified Repository Pattern**: M365 + Google + Shadow calendars as single stream
- **Zero-Backend**: All data local, OAuth direct to providers

---

**Built with ❤️ for neurodivergent users**
