# Maps and Geocoding Documentation

## Provider & Attribution
- **Map Renderer:** OSMDroid (`osmdroid-android:6.1.18`) rendering OpenStreetMap `MAPNIK` tiles.
- **User Agent:** Set to `com.pingoo.app` in `PingooApplication` for OSM policy compliance.
- **Reverse Geocoding:** `GeocodingHelper` using Android `Geocoder` with asynchronous listener and offline fallback formatting `Coordinates (lat, lng)`.
- **Licensing:** Free open-source OpenStreetMap tile data under ODbL.
