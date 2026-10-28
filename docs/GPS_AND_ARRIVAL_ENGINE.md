# GPS and Arrival Engine

## Core Components
1. `HaversineDistanceCalculator`: Calculates geodesic surface distance in meters using Earth radius $R = 6,371,000$ m.
2. `ArrivalStateMachine`: Evaluates live GPS fixes with accuracy thresholding ($< 100$m), fix staleness rejection ($< 60$s), and 20m boundary hysteresis.
3. `LocationTrackingService`: Android Foreground Service registering `LocationManager` updates and broadcasting live status.
