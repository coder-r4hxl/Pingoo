# Security and Privacy Overview

## Principles
1. **On-Device Location Processing:** All coordinate distance calculations occur locally using pure Kotlin Haversine algorithms.
2. **Zero Third-Party Cloud Uploads:** No location history, coordinates, or user data are uploaded to cloud servers.
3. **No Tracking / Analytics:** Zero advertising SDKs, tracking libraries, or analytics.
4. **Least Privilege Permissions:** Only minimal required location, foreground service, notification, and vibration permissions are declared.
5. **Component Protection:** Non-launcher activities (`AlarmActivity`) and services (`LocationTrackingService`) are explicitly marked `exported="false"`.
