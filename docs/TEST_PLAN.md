# Pingoo Test Plan

## Unit Testing Strategy

### Executed Unit Tests
Unit tests are executed via `./gradlew testDebugUnitTest`.

1. **App Update Engine (`AppUpdateEngineTest`):**
   - Verified candidate version code comparison (newer code triggers update available, equal/older version codes rejected).
   - Verified HTTPS APK URL validation (secure HTTPS accepted, HTTP and non-APK extensions rejected).

2. **End-To-End Journey Integration (`EndToEndJourneyIntegrationTest`):**
   - Verified multi-stop journey progression from TRACKING -> APPROACHING -> ARRIVED -> ACKNOWLEDGED -> ADVANCED -> COMPLETED.
   - Verified rejection of inaccurate GPS fixes (> 100m accuracy).
   - Verified process recreation state restoration.

3. **Journey History (`JourneyHistoryTest`):**
   - Verified creation and field values for completed and interrupted journey history items.

4. **Map Integration (`MapIntegrationTest`):**
   - Verified valid destination validation for coordinates picked from interactive map tap.

5. **Multi-Stop Engine (`MultiStopEngineTest`):**
   - Verified active stop tracking, arrival detection, and stop advancement upon acknowledgment.

6. **Alarm Manager Engine (`AlarmManagerEngineTest`):**
   - Verified alert allowed state, snooze suppression, and arrival acknowledgment.

7. **Arrival State Machine (`ArrivalStateMachineTest`):**
   - Verified IDLE -> TRACKING -> APPROACHING -> ARRIVED state transitions, accuracy filtering, and boundary hysteresis.

8. **Haversine Distance Calculator (`HaversineDistanceCalculatorTest`):**
   - Verified 0m distance, short distances, long distances, and antipodal coordinates.

9. **Distance Formatter (`DistanceFormatterTest`):**
   - Verified meter, kilometer, and accuracy formatting in English and Hindi.
