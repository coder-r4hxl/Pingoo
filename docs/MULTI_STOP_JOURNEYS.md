# Multi-Stop Journeys Documentation

## Engine Logic
- `MultiStopEngine`: Manages ordered stops (`JourneyStop`) in a multi-stop journey (`MultiStopJourney`).
- Tracks active stop index and calculates remaining distance.
- Automatically advances to subsequent stops upon arrival acknowledgment.
- Prevents duplicate alerts and premature stop skipping.
