# Pingoo

> **Your Journey. Your Arrival. Your Alert.**

Pingoo is a native Android GPS-based multi-destination alarm and travel companion designed for offline-first reliability during commutes on trains, buses, and road trips.

---

## Technical Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** MVVM / Clean Architecture (UI, Domain, Data, Service layers)
- **State Machine:** Deterministic `ArrivalStateMachine` (`IDLE`, `TRACKING`, `APPROACHING`, `ARRIVED`, `PAUSED`, `GPS_UNAVAILABLE`)
- **Background Engine:** Android Foreground Service with `foregroundServiceType="location"`
- **Database:** Room Database (Offline local storage)
- **Preferences:** Jetpack DataStore Preferences
- **Asynchrony:** Kotlin Coroutines & StateFlow
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34 (Android 14)
- **Package Name:** `com.pingoo.app`

---

## Project Structure

```text
com.pingoo.app/
├── data/
│   ├── datastore/            # DataStore for user preferences (language, vibration)
│   ├── local/                # Room database, DAO, entities
│   └── repository/           # Repository implementations
├── domain/
│   ├── engine/               # ArrivalStateMachine for journey tracking
│   ├── model/                # Core domain entities (Destination, Settings, Language, GpsFix)
│   ├── repository/           # Repository interfaces
│   ├── usecase/              # Business validation use cases
│   └── util/                 # Haversine distance calculator & DistanceFormatter
├── service/                  # LocationTrackingService (Foreground location service)
├── ui/
│   ├── i18n/                 # AppStrings and English/Hindi translations
│   ├── navigation/           # Screen routing
│   ├── screens/              # Jetpack Compose screens (Home, Destinations, Journey, History, Settings)
│   ├── theme/                # Material 3 colors and theme
│   └── viewmodel/            # MainViewModel
├── MainActivity.kt           # App entry point with Bottom Navigation
└── PingooApplication.kt      # Application class
```

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
