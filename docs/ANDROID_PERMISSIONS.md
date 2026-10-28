# Android Permissions Manifest Summary

| Permission | Purpose |
|---|---|
| `INTERNET` | Render OpenStreetMap map tiles via OSMDroid |
| `ACCESS_NETWORK_STATE` | Check connectivity for tile loading |
| `ACCESS_FINE_LOCATION` | Accurate GPS coordinates for distance engine |
| `ACCESS_COARSE_LOCATION` | Network location fallback |
| `FOREGROUND_SERVICE` | Run background journey tracking service |
| `FOREGROUND_SERVICE_LOCATION` | Required on Android 14 for location foreground services |
| `POST_NOTIFICATIONS` | Required on Android 13+ for tracking notifications |
| `VIBRATE` | Trigger alarm vibration on destination arrival |
