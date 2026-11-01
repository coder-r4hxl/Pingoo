# Security Test Plan

## Automated Security Checks
1. **Cleartext Traffic Enforcement**: Verify `network_security_config.xml` has `cleartextTrafficPermitted="false"`.
2. **CSV Formula Injection**: Execute `SecurityAndReliabilityTest.testCsvFormulaInjectionSanitization()` to ensure dangerous prefixes (`=`, `+`, `-`, `@`, `\t`) are neutralised.
3. **Round-Trip Alert Isolation**: Execute `SecurityAndReliabilityTest.testRoundTripArrivalOccurrenceIndependence()` to verify multiple visits to the same destination generate isolated alerts.
4. **Foreign Key Integrity**: Execute `MIGRATION_4_5` verification tests to confirm destination deletion does not violate constraints.

## Manual Physical-Device Verification Checklist
- [ ] Install debug APK on physical Android 10+ and Android 13+ devices.
- [ ] Start background tracking, lock screen, and verify high-importance arrival alert displays snooze and dismiss buttons.
- [ ] Export journey history CSV and open in Google Sheets / Excel; confirm formulas do not execute.
- [ ] Test decimal-comma locale (e.g. French, German) when entering coordinates.
- [ ] Switch between English and Hindi; verify UI and arrival alerts update immediately.
