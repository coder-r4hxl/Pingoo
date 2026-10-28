# Google AI Studio Workflow Guide

This guide describes how to maintain and enhance Pingoo safely using Google AI Studio and Gemini models:
1. Always run `./gradlew testDebugUnitTest` before and after prompt execution.
2. Verify Room database schema migration compatibility whenever modifying entities.
3. Preserve non-exported component flags in `AndroidManifest.xml`.
