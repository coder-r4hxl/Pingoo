# Pingoo In-App Updater Documentation

## Overview
Pingoo includes a secure, free-to-operate in-app update mechanism designed to query stable release manifests from GitHub Releases (`coder-r4hxl/Pingoo`).

## Features
1. **Version Code Comparison:** `AppUpdateEngine` compares candidate release version codes against the installed `versionCode`. Updates are triggered only when the candidate version code is greater.
2. **HTTPS URL Enforcement:** Only secure `https://` URLs ending in `.apk` are accepted for downloading.
3. **SHA-256 Integrity Verification:** Downloads are verified against an expected SHA-256 hash before installation is allowed.
4. **Android FileProvider Integration:** Serves downloaded update APKs via Android's `FileProvider` (`content://com.pingoo.app.fileprovider/cache-path/apk_updates/...`) to launch the native Android Package Installer safely.
5. **Localization:** Statuses, progress bars, and installation buttons are fully translated in English and Hindi.
