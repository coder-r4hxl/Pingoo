# Pingoo Project Overview

Pingoo is a native Android GPS-based multi-destination alarm and travel companion app designed for commuters on trains, buses, and road trips. It ensures offline-first reliability, clear multi-language support (English & Hindi), and privacy-first local storage.

## Core Pillars
1. **Offline-First Geodesic Distance Engine:** Uses pure Kotlin Haversine distance calculations without cloud dependencies.
2. **Deterministic Arrival State Machine:** Manages transitions across `IDLE`, `TRACKING`, `APPROACHING`, `ARRIVED`, `PAUSED`, and `GPS_UNAVAILABLE` with accuracy filtering and boundary hysteresis.
3. **Android Location Foreground Service:** Operates as a compliant foreground service (`foregroundServiceType="location"`) with ongoing progress notifications.
4. **Lock-Screen Full-Screen Overlay Alarm:** Launches `AlarmActivity` on arrival with audio ringtone playback, repeating vibration, snooze (5/10/15 mins), and dismissal.
5. **Ordered Multi-Stop Route System:** Enables multi-stop train/bus journeys with automatic progression upon arrival acknowledgment.
6. **OpenStreetMap Integration:** Uses OSMDroid (`osmdroid-android:6.1.18`) for billing-free interactive map destination picking and geocoding.
7. **Local Journey History & Settings:** Room Database v3 stores destinations, journeys, and history locally; DataStore stores user preferences.
