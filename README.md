# JEDA Android

Android client untuk prototype JEDA (Digital Wellness & Behavioral Interception System).

## Setup

1. Install Tailscale di HP dan laptop
2. Pastikan keduanya terhubung ke tailnet yang sama
3. Open project di Android Studio
4. Sync Gradle
5. Run pada device

## Requirements

- Android Studio Hedgehog | 2023.1.1 atau lebih baru
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 35 (Android 16)
- Kotlin 2.0
- Jetpack Compose

## Backend Connection

App terhubung ke backend via Tailscale menggunakan hostname `jeda-backend:8000`.

Untuk mengubah backend URL:
1. Edit `MainActivity.kt`
2. Ubah URL di fungsi `testBackendConnection()`

Alternatif: gunakan Tailscale IP langsung:
```kotlin
.url("http://100.x.x.x:8000/health")
```

## Project Structure

```
jeda-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/jeda/android/
│   │   │   ├── MainActivity.kt
│   │   │   ├── JedaApplication.kt
│   │   │   └── ui/theme/
│   │   └── res/
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Milestones

- **M0** (current): Baseline + backend connectivity test ✓
- **M1**: Monitoring lifecycle + AccessibilityService
- **M2**: FDS behavioral sensing
- **M3**: Target app filtering
- **M4**: MediaProjection + frame capture
- **M5**: Backend visual analysis integration
- **M6**: RRS aggregation
- **M7**: Intervention UI
- **M8**: Testing dan polish

## Build

```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Testing Backend Connection

1. Pastikan backend berjalan di laptop
2. HP dan laptop terhubung via Tailscale
3. Buka app
4. Tap "Test Backend Connection"
5. Expected: "✓ Connected" dengan JSON response

## Notes

- M0 hanya baseline connectivity test
- AccessibilityService, MediaProjection, dan monitoring dimulai di M1+
- App ini prototype akademik, bukan production-ready
