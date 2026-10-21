# Pingoo Test Plan

## Unit Testing Strategy

### Executed Unit Tests
Unit tests are executed via `./gradlew testDebugUnitTest`.

1. **Alarm Manager Engine (`AlarmManagerEngineTest`):**
   - Verified initial alert permission state.
   - Verified alert suppression after arrival acknowledgment.
   - Verified snooze duration suppression (alert suppressed during 5-minute window, allowed after expiry).
   - Verified alert reset behavior.

2. **Arrival State Machine (`ArrivalStateMachineTest`):**
   - Verified IDLE -> TRACKING -> APPROACHING -> ARRIVED state transitions.
   - Verified zero distance and exact boundary arrivals.
   - Verified filtering of inaccurate GPS fixes (> 100m accuracy) and stale fixes (> 60s old).
   - Verified PAUSED, RESUMED, STOPPED, and GPS_UNAVAILABLE transitions.

3. **Haversine Distance Calculator (`HaversineDistanceCalculatorTest`):**
   - Verified 0m distance for identical coordinates.
   - Verified known cross-country distance (Delhi to Mumbai ~1,148 km).
   - Verified short distances (~111 m) and extreme/antipodal coordinates.

4. **Distance Formatter (`DistanceFormatterTest`):**
   - Verified meter / kilometer / accuracy formatting in English and Hindi.

5. **Destination Validation (`DomainUnitTests`):**
   - Verified destination validation bounds and language code resolution.
