# Security Remediation Tracker

| Finding ID | Title | Status | Changed Files | Regression Tests |
|---|---|---|---|---|
| SEC-01 | Cleartext Traffic Permitted | VERIFIED | `network_security_config.xml`, `AndroidManifest.xml` | Build & packaging verification |
| SEC-02 | Unintended Cloud Backup of Database | VERIFIED | `data_extraction_rules.xml`, `AndroidManifest.xml` | Manifest merge verification |
| SEC-03 | CSV Formula Injection (CWE-1236) | VERIFIED | `HistoryExportHelper.kt` | `SecurityAndReliabilityTest.testCsvFormulaInjectionSanitization` |
| SEC-04 | Foreign Key Constraint Deletion Crash | VERIFIED | `AppDatabase.kt`, `JourneyEntities.kt` | `MIGRATION_4_5` verification |
| SEC-05 | Background Alert & Loop Block | VERIFIED | `LocationTrackingService.kt`, `AndroidManifest.xml` | `Phase6RegressionTest` |
| SEC-06 | Round-Trip Arrival Suppression | VERIFIED | `AlarmManagerEngine.kt` | `SecurityAndReliabilityTest.testRoundTripArrivalOccurrenceIndependence` |
| SEC-07 | Packaging Resource Exclusion Typo | VERIFIED | `build.gradle.kts` | `./gradlew assembleDebug` |
| SEC-08 | Incomplete Clear All Data Wipe | VERIFIED | `MainViewModel.kt`, `MainActivity.kt` | `MainViewModelTest` |
