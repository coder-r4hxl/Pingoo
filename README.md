# Pingoo

> **Your Journey. Your Arrival. Your Alert.**

Pingoo is a native Android GPS-based multi-destination alarm and travel companion designed for offline-first reliability during commutes on trains, buses, and road trips.

---

## Technical Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Map View:** OSMDroid (`osmdroid-android:6.1.18` - OpenStreetMap free tile rendering)
- **Architecture:** MVVM / Clean Architecture (UI, Domain, Data, Service layers)
- **Multi-Stop Engine:** Deterministic `MultiStopEngine` for ordered train/bus routes
- **Background Engine:** Android Foreground Service with `foregroundServiceType="location"`
- **Database:** Room Database v3 (`destinations`, `journeys`, `journey_stops`, `journey_history`)
- **Preferences:** Jetpack DataStore Preferences
- **Asynchrony:** Kotlin Coroutines & StateFlow
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34 (Android 14)
- **Package Name:** `com.pingoo.app`

---

## Setup & Build Instructions

### Prerequisites
- JDK 17 or higher (JDK 21 tested)
- Android SDK (API 34)

### Commands
- **Run Unit Tests:**
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Run Android Lint:**
  ```bash
  ./gradlew lintDebug
  ```
- **Build Debug APK:**
  ```bash
  ./gradlew assembleDebug
  ```
- **Generated APK Location:**
  `app/build/outputs/apk/debug/app-debug.apk`

---

## Languages
Pingoo supports instant runtime switching between **English** and **Hindi (हिन्दी)** from the Settings screen.

---

## Documentation
- [Architecture & Design (`docs/ARCHITECTURE.md`)](docs/ARCHITECTURE.md)
- [Permissions & Privacy (`docs/PERMISSIONS_AND_PRIVACY.md`)](docs/PERMISSIONS_AND_PRIVACY.md)
- [Test Plan (`docs/TEST_PLAN.md`)](docs/TEST_PLAN.md)
- [Phase Status (`docs/PHASE_STATUS.md`)](docs/PHASE_STATUS.md)
- [Dependencies & Licenses (`DEPENDENCIES.md`)](DEPENDENCIES.md)
