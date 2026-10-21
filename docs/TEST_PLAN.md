# Pingoo Test Plan

## Unit Testing Strategy

### Executed Unit Tests
Unit tests are executed via `./gradlew testDebugUnitTest`.

1. **Journey History (`JourneyHistoryTest`):**
   - Verified creation and field values for completed journey history items.
   - Verified field values and status for interrupted/stopped journey history items.

2. **Map Integration (`MapIntegrationTest`):**
   - Verified valid destination validation for coordinates picked from interactive map tap.
   - Verified error handling for out-of-bounds map pin coordinates.

3. **Multi-Stop Engine (`MultiStopEngineTest`):**
   - Verified initial tracking of the first stop in an ordered route.
   - Verified active stop tracking, arrival detection, and stop advancement upon acknowledgment.
   - Verified journey completion upon acknowledging the final stop.

4. **Alarm Manager Engine (`AlarmManagerEngineTest`):**
   - Verified alert allowed state, snooze suppression, and arrival acknowledgment.

5. **Arrival State Machine (`ArrivalStateMachineTest`):**
   - Verified IDLE -> TRACKING -> APPROACHING -> ARRIVED state transitions, accuracy filtering, and boundary hysteresis.

6. **Haversine Distance Calculator (`HaversineDistanceCalculatorTest`):**
   - Verified 0m distance, short distances, long distances, and antipodal coordinates.

7. **Distance Formatter (`DistanceFormatterTest`):**
   - Verified meter, kilometer, and accuracy formatting in English and Hindi.
