# Pingoo Test Plan

## Unit Testing Strategy

### Executed Unit Tests
Unit tests are executed via `./gradlew testDebugUnitTest`.

1. **End-To-End Journey Integration (`EndToEndJourneyIntegrationTest`):**
   - Verified multi-stop journey progression from TRACKING -> APPROACHING -> ARRIVED -> ACKNOWLEDGED -> ADVANCED -> COMPLETED.
   - Verified rejection of inaccurate GPS fixes (> 100m accuracy).
   - Verified process recreation state restoration.

2. **Journey History (`JourneyHistoryTest`):**
   - Verified creation and field values for completed and interrupted journey history items.

3. **Map Integration (`MapIntegrationTest`):**
   - Verified valid destination validation for coordinates picked from interactive map tap.
   - Verified error handling for out-of-bounds map pin coordinates.

4. **Multi-Stop Engine (`MultiStopEngineTest`):**
   - Verified active stop tracking, arrival detection, and stop advancement upon acknowledgment.

5. **Alarm Manager Engine (`AlarmManagerEngineTest`):**
   - Verified alert allowed state, snooze suppression, and arrival acknowledgment.

6. **Arrival State Machine (`ArrivalStateMachineTest`):**
   - Verified IDLE -> TRACKING -> APPROACHING -> ARRIVED state transitions, accuracy filtering, and boundary hysteresis.

7. **Haversine Distance Calculator (`HaversineDistanceCalculatorTest`):**
   - Verified 0m distance, short distances, long distances, and antipodal coordinates.

8. **Distance Formatter (`DistanceFormatterTest`):**
   - Verified meter, kilometer, and accuracy formatting in English and Hindi.

## Manual Real-Device Testing Checklist
- [ ] Install APK on Android 12, 13, and 14 devices.
- [ ] Grant location and notification permissions.
- [ ] Build a multi-stop route and start journey tracking.
- [ ] Verify ongoing foreground notification displays current stop and live distance.
- [ ] Simulate or walk into arrival radius and confirm ringtone and vibration trigger.
- [ ] Tap "Snooze" on full-screen lock overlay and verify 5-minute suppression.
- [ ] Tap "Acknowledge & Next Stop" and verify automatic advancement to the next stop.
