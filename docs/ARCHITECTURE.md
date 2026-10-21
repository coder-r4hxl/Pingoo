# Pingoo Architecture Documentation

## Overview
Pingoo follows Clean Architecture and MVVM (Model-View-ViewModel) principles to ensure strict separation of concerns, testability, and offline reliability.

```
+-------------------------------------------------------------+
|                      UI Layer                               |
|   Compose Screens + Multi-Stop Builder + AlarmActivity     |
|   MainViewModel + AppStrings (i18n)                         |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                     Domain Layer                            |
|   Models (Destination, MultiStopJourney, JourneyStop, GpsFix)|
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
|   AlarmPlayer (Ringtone & Vibration Engine)                |
|   Room AppDatabase (destinations, journeys, journey_stops)  |
|   DataStore UserPreferencesRepository                       |
+-------------------------------------------------------------+
```

## Phase 4 Architecture: Multi-Destination & Multi-Stop Journey System

### 1. MultiStopEngine (`com.pingoo.app.domain.engine.MultiStopEngine`)
- Controls ordered multi-stop journeys (e.g. train or bus routes with consecutive stops).
- Tracks distance and arrival state for the current active stop (`activeStopIndex`).
- Automatically advances to the next stop when the user acknowledges arrival at the current stop.
- Completes the journey upon acknowledging the final stop.

### 2. Room Database Schema v2 (`com.pingoo.app.data.local.AppDatabase`)
- **`journeys` table:** Stores journey metadata (`title`, `activeStopIndex`, `isCompleted`).
- **`journey_stops` table:** Stores stop sequences linked via foreign key to `journeys` and `destinations`.
