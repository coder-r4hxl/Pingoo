# Pingoo Test Plan

## Unit Testing Strategy

### Executed Unit Tests
Unit tests are executed via `./gradlew testDebugUnitTest`.

1. **Multi-Stop Engine (`MultiStopEngineTest`):**
   - Verified initial tracking of the first stop in an ordered route.
   - Verified active stop tracking and arrival detection.
   - Verified stop advancement upon arrival acknowledgment.
   - Verified journey completion upon acknowledging the final stop.
   - Verified pause, resume, and stop controls.

2. **Alarm Manager Engine (`AlarmManagerEngineTest`):**
   - Verified alert allowed state, snooze suppression, and arrival acknowledgment.

3. **Arrival State Machine (`ArrivalStateMachineTest`):**
   - Verified IDLE -> TRACKING -> APPROACHING -> ARRIVED state transitions, accuracy filtering, and boundary hysteresis.

4. **Haversine Distance Calculator (`HaversineDistanceCalculatorTest`):**
   - Verified 0m distance, short distances, long distances, and antipodal coordinates.

5. **Distance Formatter (`DistanceFormatterTest`):**
   - Verified meter, kilometer, and accuracy formatting in English and Hindi.
