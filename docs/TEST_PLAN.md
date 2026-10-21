# Pingoo Test Plan

## Unit Testing Strategy

### Executed Tests
Unit tests run using `./gradlew testDebugUnitTest`.

1. **Haversine Distance Calculator (`HaversineDistanceCalculatorTest`):**
   - Verified 0m distance for identical start/end points.
   - Verified known distance calculation (New Delhi to Mumbai ~ 1,148 km).
   - Verified short-distance accuracy (~111 meters).

2. **Destination Validation (`DomainUnitTests`):**
   - Tested valid destination inputs.
   - Tested empty destination name error handling.
   - Tested out-of-bound latitude/longitude handling.
   - Tested radius constraint validation (arrival radius vs advance warning radius).

3. **Language/i18n Model (`DomainUnitTests`):**
   - Verified locale code resolution for English, Hindi, uppercase codes, and unknown fallbacks.

## Static Analysis & Quality Checks
- Executed `./gradlew lintDebug` to verify Android resource linking, manifest correctness, and code hygiene.
- Executed `./gradlew assembleDebug` to confirm build artifacts generation (`app-debug.apk`).
