# Pingoo Test Plan

## Unit Testing Strategy

### Executed Unit Tests
Unit tests are executed via `./gradlew testDebugUnitTest`.

1. **Haversine Distance Calculator (`HaversineDistanceCalculatorTest`):**
   - Verified 0m distance for identical start/end points.
   - Verified known distance calculation (New Delhi to Mumbai ~ 1,148 km).
   - Verified short-distance accuracy (~111 meters).
   - Verified extreme coordinates (Equator-to-Pole ~10,000 km, across International Date Line).

2. **Arrival State Machine (`ArrivalStateMachineTest`):**
   - Verified IDLE -> TRACKING transition upon journey start.
   - Verified progress from TRACKING -> APPROACHING (within warning radius) -> ARRIVED (within arrival radius).
   - Verified zero distance and exact boundary arrivals.
   - Verified filtering of inaccurate GPS fixes (> 100m accuracy) and stale fixes (> 60s old).
   - Verified PAUSED, RESUMED, STOPPED, and GPS_UNAVAILABLE state transitions.
   - Verified handling of invalid coordinate bounds.

3. **Distance Formatter (`DistanceFormatterTest`):**
   - Verified meter formatting ("450 m" / "450 मी.").
   - Verified kilometer formatting ("12.4 km" / "12.4 कि.मी.").
   - Verified accuracy formatting ("±15 m" / "±15 मी.").
   - Verified null/negative/NaN fallback ("--").

4. **Destination Validation (`DomainUnitTests`):**
   - Verified valid destination parameters.
   - Verified error handling for empty names, out-of-bounds coordinates, and invalid radiuses.
   - Verified language code resolution.

## Manual Real-Device Testing Checklist (Phase 2)
- [ ] Grant location permissions on Android 12/13/14 device.
- [ ] Start a journey and verify persistent foreground notification appears.
- [ ] Move toward target destination and verify live distance updates on Journey screen and notification.
- [ ] Toggle GPS off in Android quick settings and verify state changes to `GPS_UNAVAILABLE`.
- [ ] Pause and resume journey tracking from UI.
- [ ] Stop journey and verify service stops and notification is removed.
