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
|   Room AppDatabase with Safe Migrations (1->2, 2->3)        |
|   DataStore UserPreferencesRepository                       |
+-------------------------------------------------------------+
```

## Security & Android Behavior Hardening

### 1. Component Protections & Least Privilege
- Internal Android components (`AlarmActivity`, `LocationTrackingService`) are explicitly marked as `android:exported="false"`.
- Requests minimal permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, `POST_NOTIFICATIONS`, `VIBRATE`, `INTERNET`, `ACCESS_NETWORK_STATE`).

### 2. Safe Room Migrations (`AppDatabase`)
- Explicit migration paths `MIGRATION_1_2` and `MIGRATION_2_3` ensure zero data loss during app updates.
