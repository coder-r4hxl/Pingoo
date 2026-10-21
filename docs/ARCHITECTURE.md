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
|   OSMDroid Tile Engine (OpenStreetMap tile renderer)        |
|   AlarmPlayer (Ringtone & Vibration Engine)                |
|   Room AppDatabase (destinations, journeys, journey_stops)  |
|   DataStore UserPreferencesRepository                       |
+-------------------------------------------------------------+
```

## Phase 5 Architecture: Map & Geocoding Integration

### 1. OSMDroid Map Renderer (`com.pingoo.app.ui.map.OsmMapView`)
- Utilizes open-source `osmdroid-android:6.1.18` rendering engine with `TileSourceFactory.MAPNIK`.
- Complies with OpenStreetMap tile usage guidelines by configuring user agent application ID (`com.pingoo.app`).
- Enables single-tap coordinate pin placement (`MapEventsOverlay`) for visual destination selection.

### 2. Reverse Geocoding (`com.pingoo.app.ui.map.GeocodingHelper`)
- Asynchronously converts map latitude/longitude coordinates into human-readable address lines using Android's native `Geocoder`.
- Features graceful offline fallback formatting raw coordinates `Coordinates (lat, lng)` if network or geocoding services are unavailable.
