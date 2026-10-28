# Known Issues and Backlog

## Known Android & OEM Limitations
1. **OEM Battery Optimization Restrictions:** Custom vendor battery saver policies (e.g. MIUI, OneUI) may throttle background GPS fixes if battery optimization is enforced by the user.
2. **Cold GPS Fix Warm-up:** Indoors or in dense urban canyons, cold GPS startup may take 10-30 seconds for initial fine accuracy acquisition (< 100m).
