# Device Compatibility Guide

## Supported Android Versions
- **Minimum SDK:** 26 (Android 8.0 Oreo)
- **Target SDK:** 34 (Android 14 Upside Down Cake)

## Foreground Service & Location Restrictions
- **Android 14 (API 34):** Requires `FOREGROUND_SERVICE_LOCATION` permission and explicit `ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION` flag when invoking `startForeground()`. Pingoo strictly complies.
- **Android 13 (API 33):** Requires runtime `POST_NOTIFICATIONS` permission. Pingoo prompts the user with an explanatory dialog before journey start.
- **Android 10+ (API 29+):** Background `startActivity()` is restricted. Pingoo handles this by issuing a high-importance heads-up notification with `setFullScreenIntent` and action buttons (Snooze, Dismiss, Stop), ensuring controls are always accessible.

## OEM Battery Management Considerations
Aggressive OEM battery optimizers (such as MIUI/HyperOS, OneUI, EMUI) may kill background services if not configured:
- Users should be advised to set Battery Usage to "Unrestricted" for Pingoo if extended multi-hour background tracking is desired.
