# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

We take the security of Pingoo seriously. If you discover a security vulnerability, please report it privately:

1. **Email**: Open a security advisory through the GitHub repository rather than public issues.
2. **Details**: Include reproducible steps, affected components, and potential impact.
3. **Response Time**: We acknowledge reports within 48 hours and provide a remediation plan within 5 business days.

## Security Architecture & Core Assumptions

- **Local-First & On-Device Storage**: Pingoo operates 100% on-device. Saved destinations, coordinates, and journey history are stored in local Room SQLite database and DataStore files.
- **No Third-Party Analytics or Cloud Uploads**: Location data is never uploaded to external analytics servers or remote cloud profiles.
- **Component Sandboxing**: All background services and activities are explicitly non-exported (`android:exported="false"`), preventing arbitrary intent invocation from external apps.
- **Cleartext Traffic Disabled**: Network communication strictly enforces HTTPS TLS certificate validation via `network-security-config`.
- **Physical Device & Root Limits**: Local application data cannot be guaranteed secure against users with physical root access or unlocked compromised devices.
