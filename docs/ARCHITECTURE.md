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
|   Models (Destination, UserSettings, Language)              |
|   Use Cases (ValidateDestinationUseCase)                    |
|   Calculators (HaversineDistanceCalculator)                 |
|   Repository Contracts                                      |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                      Data Layer                             |
|   Room AppDatabase & DestinationDao                         |
|   DataStore UserPreferencesRepository                       |
|   Repository Implementations                                |
+-------------------------------------------------------------+
```

## Layers

### 1. Domain Layer (`com.pingoo.app.domain`)
- Independent of Android framework classes.
- Pure Kotlin business logic and models.
- Contains `HaversineDistanceCalculator` for offline geodesic distance computation.
- Contains validation rules (`ValidateDestinationUseCase`).

### 2. Data Layer (`com.pingoo.app.data`)
- Implements persistence using Room DB for destinations (`destinations` table).
- Implements preference storage via Jetpack DataStore Preferences (`user_settings`).
- Exposes Kotlin Flows to provide continuous reactive state updates.

### 3. UI Layer (`com.pingoo.app.ui`)
- Fully built with Jetpack Compose and Material 3.
- `MainViewModel` manages screen states and bridges domain/data to composables.
- Supports runtime language switching between English and Hindi (`AppStrings`).

### 4. Service Layer Contract (`com.pingoo.app.service`)
- `LocationServiceContract` defines the contract for Phase 2 background foreground location tracking service.
