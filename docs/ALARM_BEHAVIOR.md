# Alarm Behavior Documentation

## Alarm Components
- `AlarmPlayer`: Audio ringtone playback using `USAGE_ALARM` audio attributes and repeating waveform vibration.
- `AlarmManagerEngine`: Manages 5-minute snooze timer suppression and full arrival acknowledgment.
- `AlarmActivity`: Full-screen lock overlay with `showWhenLocked="true"` and Compose arrival UI in English and Hindi.
