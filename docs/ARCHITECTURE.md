# Pingoo Architecture Documentation

## Overview
Pingoo follows Clean Architecture and MVVM (Model-View-ViewModel) principles to ensure strict separation of concerns, testability, and offline reliability.

```
+-------------------------------------------------------------+
|                      UI Layer                               |
|   Compose Screens (Home, Destinations, Journey, History,    |
|   Settings) + MainViewModel + AppStrings (i18n)              |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                     Domain Layer                            |
|   Models (Destination, UserSettings, Language, GpsFix)      |
|   State Engine (ArrivalStateMachine)                        |
|   Calculators (HaversineDistanceCalculator)                 |
|   Utilities (DistanceFormatter)                             |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|               Data & Service Layers                         |
|   LocationTrackingService (Android Foreground Service)      |
|   Room AppDatabase & DestinationDao                         |
|   DataStore UserPreferencesRepository                       |
+-------------------------------------------------------------+
```

## Phase 2 Architecture: GPS Tracking & Arrival Engine

### 1. Domain Layer (`com.pingoo.app.domain`)
- **ArrivalStateMachine:** Handles deterministic transitions across states (`IDLE`, `TRACKING`, `APPROACHING`, `ARRIVED`, `PAUSED`, `GPS_UNAVAILABLE`).
- **Hysteresis & Filtering:** Rejects GPS fixes with accuracy worse than 100 meters or stale fixes (> 60 seconds). Includes a 20m hysteresis buffer at radius boundaries to prevent jitter.
- **DistanceFormatter:** Formats distances in readable units (meters / km) with localized English and Hindi text.

### 2. Service Layer (`com.pingoo.app.service.LocationTrackingService`)
- Android Foreground Service with `foregroundServiceType="location"`.
- Displays a ongoing foreground notification with current journey state and live distance updates.
- Registers location updates using `LocationManager` (GPS/Network providers).
- Exposes `serviceEvaluation` `StateFlow` bound to `MainViewModel`.
