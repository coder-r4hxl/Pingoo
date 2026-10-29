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
|                  AlarmManagerEngine, AppUpdateEngine)       |
|   Calculators (HaversineDistanceCalculator)                 |
|   Utilities (DistanceFormatter)                             |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|               Data & Service Layers                         |
|   LocationTrackingService (Foreground Service)              |
|   ApkDownloader (Secure HTTPS Update Downloader)            |
|   OSMDroid Tile Engine (OpenStreetMap tile renderer)        |
|   AlarmPlayer (Ringtone & Vibration Engine)                |
|   Room AppDatabase with Safe Migrations (1->2, 2->3)        |
|   DataStore UserPreferencesRepository                       |
+-------------------------------------------------------------+
```

## Phase 10 Architecture: Secure In-App Updater

### 1. AppUpdateEngine (`com.pingoo.app.domain.updater.AppUpdateEngine`)
- Compares candidate release version codes against installed `versionCode`.
- Enforces HTTPS URLs and verifies SHA-256 digests prior to triggering installer intents.

### 2. FileProvider & Intent Installation (`com.pingoo.app.service.updater.ApkDownloader`)
- Downloads update APKs into app-private cache directory (`cacheDir/apk_updates`).
- Generates `content://com.pingoo.app.fileprovider/...` Content URIs via Android `FileProvider` for `ACTION_VIEW` package installer execution.
