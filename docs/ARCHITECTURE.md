# Pingoo Architecture Documentation

## Overview
Pingoo follows Clean Architecture and MVVM (Model-View-ViewModel) principles to ensure strict separation of concerns, testability, and offline reliability.

```
+-------------------------------------------------------------+
|                      UI Layer                               |
|   Compose Screens + OsmMapView + GeocodingHelper            |
|   MainViewModel + AppStrings (i18n)                         |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                     Domain Layer                            |
|   Models (Destination, MultiStopJourney, JourneyHistoryItem)|
|   State Engines (MultiStopEngine, ArrivalStateMachine,      |
|                  AlarmManagerEngine)                        |
|   Calculators (HaversineDistanceCalculator)                 |
|   Utilities (DistanceFormatter)                             |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|               Data & Service Layers                         |
|   LocationTrackingService (Foreground Service)              |
|   OSMDroid Tile Engine (OpenStreetMap tile renderer)        |
|   AlarmPlayer (Ringtone & Vibration Engine)                |
|   Room AppDatabase (destinations, journeys, history)        |
|   DataStore UserPreferencesRepository                       |
+-------------------------------------------------------------+
```

## Phase 6 Architecture: Journey History, Settings & Privacy Controls

### 1. Room Database Schema v3 (`com.pingoo.app.data.local.AppDatabase`)
- **`journey_history` table:** Stores completed and stopped journey summaries (`title`, `totalStops`, `reachedStops`, `isCompleted`, `durationSeconds`, `timestampMillis`).
- Managed via `JourneyHistoryDao` with reactive Kotlin `Flow` queries and deletion operations.

### 2. Enhanced Preferences & Privacy Controls (`com.pingoo.app.data.datastore.UserPreferencesRepository`)
- Persists user preferences for language (`en`/`hi`), vibration toggle, default arrival radius, and snooze duration (5, 10, or 15 minutes).
- Privacy policy and local data deletion controls provided directly in `SettingsScreen`.
