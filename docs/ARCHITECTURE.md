# Pingoo Architecture Documentation

## Overview
Pingoo follows Clean Architecture and MVVM (Model-View-ViewModel) principles to ensure strict separation of concerns, testability, and offline reliability.

```
+-------------------------------------------------------------+
|                      UI Layer                               |
|   Compose Screens + AlarmActivity (Full-Screen Alert Overlay)|
|   MainViewModel + AppStrings (i18n)                         |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                     Domain Layer                            |
|   Models (Destination, UserSettings, Language, GpsFix)      |
|   State Engine (ArrivalStateMachine, AlarmManagerEngine)    |
|   Calculators (HaversineDistanceCalculator)                 |
|   Utilities (DistanceFormatter)                             |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|               Data & Service Layers                         |
|   LocationTrackingService (Foreground Location Service)     |
|   AlarmPlayer (Audio Ringtone & Vibration Engine)          |
|   Room AppDatabase & DestinationDao                         |
|   DataStore UserPreferencesRepository                       |
+-------------------------------------------------------------+
```

## Phase 3 Architecture: Alarm Engine & Lock-Screen Overlay

### 1. Alarm Player (`com.pingoo.app.service.alarm.AlarmPlayer`)
- Manages audio playback using `RingtoneManager` configured with `USAGE_ALARM` audio attributes for high priority alert override.
- Triggers repeating vibration patterns via `VibratorManager` (Android 12+) / `Vibrator`.

### 2. Alarm Suppression Engine (`com.pingoo.app.domain.engine.AlarmManagerEngine`)
- Prevents duplicate arrival alarms across process restarts and GPS coordinate noise.
- Supports 5-minute snooze timer suppression and full arrival acknowledgment.

### 3. Lock-Screen Overlay (`com.pingoo.app.ui.alarm.AlarmActivity`)
- Registered with `showWhenLocked="true"` and `turnScreenOn="true"`.
- Displays prominent full-screen arrival UI with immediate Snooze and Dismiss actions in English and Hindi.
- Explicitly marked as `exported="false"` for Android component security.
