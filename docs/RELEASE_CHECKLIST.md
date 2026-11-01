# Release Checklist

## 1. Code & Build Verification
- [x] All 14 automated unit and integration test suites pass (`./gradlew testDebugUnitTest`).
- [x] Zero compilation errors and zero Android Lint warnings (`./gradlew lintDebug`).
- [x] Debug APK builds successfully at `app/build/outputs/apk/debug/app-debug.apk`.
- [x] Packaging exclusions configured correctly (`/META-INF/{AL2.0,LGPL2.1}`).

## 2. Security & Privacy Controls
- [x] `android:exported="false"` verified on all internal activities and services.
- [x] `network_security_config.xml` enforces HTTPS (`cleartextTrafficPermitted="false"`).
- [x] `data_extraction_rules.xml` excludes cloud backup of local SQLite databases and preferences.
- [x] CSV export neutralises spreadsheet formula injection (`=`, `+`, `-`, `@`).
- [x] Zero secrets or keystores committed to Git history.

## 3. Pre-Release Device Checklist
- [ ] Install APK on physical Android 10, 12, 13, and 14 devices.
- [ ] Test arrival alert while screen locked and app backgrounded.
- [ ] Verify battery optimization exemptions on aggressive OEM builds (Xiaomi, Samsung, Huawei).
- [ ] Test round-trip journey (A → B → A).
- [ ] Verify runtime language toggle (English ↔ Hindi).
