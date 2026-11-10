# NetKeep 5G — Android Background Network Traffic Generator

**NetKeep 5G** is a native Android application built in Kotlin, Jetpack Compose, and Material 3. It generates controlled, genuine HTTPS network traffic directly from your phone in the background (even when the screen is off or the phone is locked) to maintain cellular socket activity and investigate its effect on hotspot availability for connected devices.

---

## 1. Project Overview & Objective

When using 5G mobile data (such as Airtel 5G in India) as a Wi-Fi hotspot, cellular base stations and radio resource control (RRC) layers often downgrade inactive connections to low-power RRC_IDLE states or drop NAT translation entries. Actively maintaining background network traffic keeps the cellular radio active and preserves IP socket state.

> **Transparency Note**: NetKeep 5G generates **actual HTTPS network traffic** using real byte transfers. It does not tamper with system routing tables, bypass carrier APN tethering restrictions, or claim to prevent carrier-enforced throttling.

---

## 2. Core Features

### Real Network Traffic Engine
- **Actual HTTPS Download Traffic**: Discards received bytes from memory buffers immediately to avoid excessive RAM consumption or disk I/O.
- **High-Precision Bandwidth Limiter**: Uses a token-bucket rate limiter to regulate continuous throughput between **1 KB/s and 50 KB/s**.
- **Accurate Statistics Tracker**: Calculates live instantaneous throughput (KB/s and kbps) using a rolling window of verified byte transfers.
- **Automatic Failure Recovery**: Handles network drops and timeouts with exponential backoff retry logic.

### Two Traffic Modes
1. **Mode 1: Keep-Alive Mode**
   - Periodically issues lightweight HTTPS pings (`204 No Content` or small status responses).
   - Default interval: 30 seconds (configurable from 5s to 300s).
   - Minimal data consumption (a few kilobytes per hour).
   - Real-time success and failure request counters.
2. **Mode 2: Continuous Download Mode**
   - Sustained download stream regulated to target speed (1 KB/s to 50 KB/s).
   - Speed presets: Very Low (1 KB/s), Low (5 KB/s), Medium (10 KB/s), High (20 KB/s), Maximum (50 KB/s), and Custom fine-tuning.
   - Live readouts distinguishing **KB/s** (Kilobytes/sec) from **kbps** (Kilobits/sec).

### Background Execution & Foreground Service
- Runs as an official Android `ForegroundService` with `FOREGROUND_SERVICE_TYPE_DATA_SYNC`.
- Persistent, non-intrusive notification displaying live speed, total data consumed, running duration, and a one-tap **Stop** action.
- Partial `WakeLock` (`PowerManager.PARTIAL_WAKE_LOCK`) prevents CPU sleep while traffic generation is active.
- Safe lifecycle management: WakeLock and connections are cleanly released when stopped or destroyed.

### Data Consumption Calculator
- Theoretical daily, hourly, and 30-day projection estimates based on the selected speed.
- Side-by-side comparison between theoretical projections and actual measured session bytes.
- High-bandwidth warning dialog before launching continuous downloads $\ge 20$ KB/s.

| Speed Preset | Bandwidth (KB/s) | Bitrate (kbps) | Daily Data Consumption (24h) |
|---|---|---|---|
| **Very Low** | 1 KB/s | 8 kbps | 86.4 MB / day |
| **Low** | 5 KB/s | 40 kbps | 432.0 MB / day |
| **Medium** | 10 KB/s | 80 kbps | 864.0 MB / day |
| **High** | 20 KB/s | 160 kbps | 1.728 GB / day |
| **Maximum** | 50 KB/s | 400 kbps | 4.320 GB / day |

### Settings & Persistence
- Preferences persisted via Jetpack **DataStore**.
- Configurable default mode, startup speed, keep-alive interval, and data usage warning limit.
- Customizable HTTPS download endpoints and keep-alive ping URLs with a single-tap "Reset to Defaults" option.
- Cumulative all-time data usage tracking with clear/reset options.
- Direct shortcut to Android Battery Optimization settings.

---

## 3. Architecture & Project Structure

```
netkeep/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/netkeep5g/app/
│   │   │   │   ├── NetKeepApp.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── TrafficMode.kt            # Mode definitions (Keep-Alive, Continuous)
│   │   │   │   │   ├── SpeedPreset.kt            # Speed presets (1..50 KB/s, kbps helpers)
│   │   │   │   │   ├── TrafficState.kt           # Immutable runtime state
│   │   │   │   │   └── ConsumptionEstimate.kt   # Mathematical usage projections
│   │   │   │   ├── engine/
│   │   │   │   │   ├── BandwidthLimiter.kt       # Token bucket throttling algorithm
│   │   │   │   │   ├── TrafficStatsTracker.kt    # Rolling window speed & byte counting
│   │   │   │   │   └── TrafficEngine.kt          # OkHttp coroutine download worker
│   │   │   │   ├── service/
│   │   │   │   │   ├── TrafficForegroundService.kt # Foreground service & WakeLock
│   │   │   │   │   └── TrafficNotificationManager.kt # Notification channel & actions
│   │   │   │   ├── data/
│   │   │   │   │   ├── UserSettings.kt           # Data model for persistent preferences
│   │   │   │   │   ├── SettingsRepository.kt     # Jetpack DataStore repository
│   │   │   │   │   └── TrafficStateRepository.kt # Reactive StateFlow state holder
│   │   │   │   └── ui/
│   │   │   │       ├── MainActivity.kt           # Runtime permissions & root setup
│   │   │   │       ├── MainViewModel.kt          # MVI/MVVM ViewModel combining state
│   │   │   │       ├── MainScreen.kt             # Navigation bar scaffold
│   │   │   │       ├── theme/                    # Charcoal dark theme & cyan accents
│   │   │   │       ├── components/               # StatusIndicator, MetricCard, SpeedSelector, WarningDialog
│   │   │   │       └── screens/                  # Dashboard, Controls, Calculator, Settings
│   │   │   └── res/                          # Vector drawables, mipmaps, strings, colors
│   │   └── test/java/com/netkeep5g/app/
│   │       ├── BandwidthLimiterTest.kt       # Unit test for rate limiting & token bucket
│   │       ├── ConsumptionCalculatorTest.kt  # Unit test for consumption formulas & table
│   │       └── TrafficStatsTrackerTest.kt    # Unit test for byte counting & session reset
│   └── build.gradle.kts
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 4. APK Path & Installation

### Direct Pre-built APK Download
The pre-built, installable APK is available in this repository:
- [`apk/NetKeep-5G-v1.0.1.apk`](apk/NetKeep-5G-v1.0.1.apk)

### Local Build APK Location
```
app/build/outputs/apk/debug/app-debug.apk
```

### Installation via ADB
Connect your phone with USB Debugging enabled, then run:
```bash
adb install -r apk/NetKeep-5G-v1.0.1.apk
```

### Direct Sideloading
1. Download or copy `apk/NetKeep-5G-v1.0.1.apk` to your phone via USB cable, Google Drive, or browser.
2. Open the file on your device and tap **Install** (allow "Install unknown apps" if prompted).

---

## 5. Android Permissions Explained

| Permission | Purpose |
|---|---|
| `INTERNET` | Required to perform actual HTTPS network downloads and keep-alive pings. |
| `ACCESS_NETWORK_STATE` | Monitors network connectivity state and Wi-Fi / cellular availability. |
| `FOREGROUND_SERVICE` | Keeps the traffic generation engine running when the application is minimized. |
| `FOREGROUND_SERVICE_DATA_SYNC` | Android 14+ (API 34) requirement for background network sync services. |
| `POST_NOTIFICATIONS` | Android 13+ (API 33) requirement to display persistent service notifications. |
| `WAKE_LOCK` | Holds a `PARTIAL_WAKE_LOCK` so the CPU does not sleep when the screen turns off. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Allows launching a prompt to exempt the app from aggressive OS task killers. |

---

## 6. Background Execution & Battery Optimization Guide

Modern Android versions (and OEM flavors like MIUI, OxygenOS, OneUI) aggressively kill background services or throttle socket polling when the screen is turned off.

To ensure uninterrupted traffic:
1. Open **NetKeep 5G > Settings**.
2. Tap **Request Battery Exemption** to open Android's battery optimization page.
3. Select **Unrestricted** (or "Don't optimize").
4. For OEM devices (Xiaomi / OnePlus / Oppo / Vivo):
   - **Auto-start**: Enable Auto-start in App Info.
   - **App Lock**: Lock NetKeep 5G in the Recent Apps overview.
   - **Battery Saver**: Set to "No restrictions".

---

## 7. Configuring Network Endpoints

NetKeep 5G allows customizing server endpoints under **Settings > Network Endpoints**:
- **Continuous Download**:
  - Default: `https://speed.cloudflare.com/__down?bytes=50000000`
  - Fallbacks: `https://proof.ovh.net/files/10Mb.dat` or any high-availability CDN file stream.
- **Keep-Alive Ping**:
  - Default: `https://connectivitycheck.gstatic.com/generate_204`
  - Fallbacks: `https://www.google.com/generate_204` or `https://cloudflare.com/cdn-cgi/trace`.
- Tap **Reset Defaults** at any time to restore the official endpoints.

---

## 8. Known Limitations

1. **Carrier Tethering Restrictions**: This tool generates legitimate cellular traffic from the phone. It does not alter TTL packets or bypass carrier hotspot entitlements.
2. **Battery Consumption**: Generating continuous network traffic and holding a Partial WakeLock prevents the CPU and modem from entering deep sleep, increasing battery usage.
3. **Android Doze & OEM Killers**: While `PARTIAL_WAKE_LOCK` and foreground services provide standard Android compliance, aggressive OEM task killers may still terminate apps under extreme memory pressure unless exempted in system settings.
