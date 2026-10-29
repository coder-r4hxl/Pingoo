# Database and Migrations Documentation

## Database Schema (AppDatabase v3)

### Tables
1. `destinations`: Saved user destinations (latitude, longitude, arrival radius, warning radius, sound, vibration).
2. `journeys`: Saved multi-stop journey metadata (`title`, `activeStopIndex`, `isCompleted`).
3. `journey_stops`: Ordered stops belonging to journeys (`journeyId`, `destinationId`, `stopOrder`, `isAcknowledged`).
4. `journey_history`: Logged journey summaries (`title`, `totalStops`, `reachedStops`, `isCompleted`, `durationSeconds`, `timestampMillis`).

## Migrations
- `MIGRATION_1_2`: Creates `journeys` and `journey_stops` tables with foreign keys and indices.
- `MIGRATION_2_3`: Creates `journey_history` table for logging past commutes.
