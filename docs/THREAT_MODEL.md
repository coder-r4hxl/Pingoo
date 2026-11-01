# Pingoo Threat Model

## 1. Assets
- **User Location & Journey History**: Geodesic coordinates, timestamps, journey names, and arrival records.
- **Application Integrity**: APK binaries, updates, Room database schema, and DataStore preferences.
- **Foreground Tracking Engine**: Foreground location service running in background.

## 2. Trust Boundaries
- **Local Application Sandbox vs. Other Apps**: Enforced via Android OS Linux UID sandboxing.
- **Application vs. External Network**: Enforced via strict HTTPS and `network_security_config.xml` (disallowing cleartext).
- **Application vs. File System**: Exported files shared only via Android `FileProvider` with temporary read permissions.

## 3. Threat Vectors & Mitigations
- **Malicious External Intents**: Mitigated by `android:exported="false"` on `AlarmActivity` and `LocationTrackingService`.
- **CSV Formula Injection**: Mitigated by sanitizing leading formula triggers (`=`, `+`, `-`, `@`, `\t`, `\r`) in `HistoryExportHelper.kt`.
- **Cloud Backup Exposure**: Mitigated by `data_extraction_rules.xml` preventing cloud backups of databases and shared preferences.
- **Unauthenticated App Updates**: Mitigated by SHA-256 integrity verification and HTTPS download checks in `AppUpdateEngine.kt`.
