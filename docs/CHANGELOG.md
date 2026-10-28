# Changelog

## [1.0.0] - Phase 1 to Phase 7 Complete Release

### Added
- Native Android Gradle project setup (`com.pingoo.app`).
- Clean Architecture (UI, Domain, Data, Service).
- Pure Kotlin `HaversineDistanceCalculator` and unit tests.
- `ArrivalStateMachine` with accuracy filtering, staleness check, and boundary hysteresis.
- `LocationTrackingService` foreground service with ongoing tracking notification.
- `AlarmPlayer` audio ringtone/vibration engine and `AlarmActivity` full-screen lock overlay.
- `MultiStopEngine` for ordered train/bus multi-stop journey progression.
- OSMDroid OpenStreetMap tile map selection (`OsmMapView`) and reverse geocoding with offline coordinate fallbacks (`GeocodingHelper`).
- Room Database v3 schema (`destinations`, `journeys`, `journey_stops`, `journey_history`) with explicit non-destructive migrations (`MIGRATION_1_2`, `MIGRATION_2_3`).
- Local journey history logging and deletion UI.
- DataStore preference storage for English/Hindi localization, vibration, and snooze duration.
- GitHub Actions CI workflow (`.github/workflows/android.yml`) uploading `Pingoo-debug-apk` artifact.
