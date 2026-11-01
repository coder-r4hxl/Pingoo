# Known Limitations & Technical Realities

1. **Reverse Engineering Protection**: No client Android application can be made 100% immune to decompilation and static analysis. R8/ProGuard obfuscates class, field, and method names, but does not provide hardware-level encryption.
2. **Physical Root Access**: If an adversary has physical root access or an unlocked bootloader, local SQLite databases stored in `/data/data/com.pingoo.app/` can be inspected.
3. **OS Force Stop**: If a user selects "Force Stop" from Android System App Settings, the OS terminates all active alarms and prevents background services from running until the user manually relaunches the app. This is an intentional Android platform behavior.
4. **Offline Map Tiles**: OpenStreetMap map tiles require network access to fetch initially. Previously viewed tiles are cached in the app cache for offline viewing. Raw GPS coordinate tracking and arrival alarms operate 100% offline.
