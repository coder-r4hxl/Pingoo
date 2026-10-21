# Pingoo Permissions & Privacy Policy

## Core Privacy Principles

1. **Offline-First Location Processing:**
   - All geographical coordinate calculations, distance measurements, and arrival state machine evaluations occur strictly on-device.
   - Pingoo does NOT transmit user locations, saved destinations, or journey state to external servers or cloud services.

2. **Required Permissions:**
   - `ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: Required for GPS/Network location fixes during journey tracking.
   - `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_LOCATION`: Required for Android 10+ and 14 foreground location tracking service.
   - `POST_NOTIFICATIONS`: Required on Android 13+ (API 33+) to show the ongoing journey tracking status notification.

3. **No Background Location Tracking Without Active Journey:**
   - Location updates are registered ONLY while a journey is actively running in the foreground service and immediately stopped when the user stops or pauses the journey.

4. **No Third-Party Analytics / Tracking:**
   - Pingoo contains zero advertising SDKs, tracking libraries, cloud location logs, or paid APIs.
