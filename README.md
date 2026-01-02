# DeviceGPT 📱

<div align="center">

![DeviceGPT](https://img.shields.io/badge/DeviceGPT-Android-blue?style=for-the-badge&logo=android)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-blue?style=for-the-badge&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-orange?style=for-the-badge)

**Comprehensive Android device monitoring and power consumption analysis**

[Features](#-features) • [Quick Start](#-quick-start) • [Contributing](#-contributing) • [Work with Teamz Lab](#-built-by-teamz-lab--need-a-similar-app)

</div>

---

## 📥 Download DeviceGPT

<div align="center">

[![Get it on Google Play](https://img.shields.io/badge/Get%20it%20on-Google%20Play-4285F4?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.teamz.lab.debugger)

[![Product Hunt](https://img.shields.io/badge/Product%20Hunt-Upvote-orange?style=for-the-badge&logo=product-hunt&logoColor=white)](https://www.producthunt.com/p/devicegpt-ai-phone-health)
[![XDA Forums](https://img.shields.io/badge/XDA%20Forums-Discuss-FF6900?style=for-the-badge&logo=xda-developers&logoColor=white)](https://xdaforums.com/t/app-devicegpt-ai-powered-android-monitor-real-data-privacy-guardian-global-leaderboard.4773593/)

**⭐ 500+ Downloads • 🔒 Privacy First • 🤖 AI-Powered**

</div>

---

## What It Does

DeviceGPT is an Android app that provides real-time device monitoring, health analysis, and power consumption insights. Built for developers, researchers, and power users who want to understand their device's performance, network behavior, and power characteristics.

**Perfect for:**
- Android developers building device-aware apps
- Researchers studying mobile power consumption
- Users who want detailed device diagnostics
- Anyone curious about their phone's performance

<div align="center">

**[📱 Download on Google Play](https://play.google.com/store/apps/details?id=com.teamz.lab.debugger)** • **[🚀 Try It](#-quick-start)** • **[📖 Build It](#-setup)** • **[🤝 Contribute](#-contributing)**

</div>

---

## Standout Features

- **Real-time Monitoring**: CPU, RAM, battery, network, and power consumption tracking via foreground service
- **Power Experiments**: Component-level power measurement (Camera, Display, CPU, Network) with CSV export
- **AI Integration**: Share device data with ChatGPT, Gemini, Claude, and other AI assistants for analysis
- **Health Scoring**: Device health metrics with daily streaks and achievement system
- **Network Testing**: Real download/upload speed tests and latency measurement

---

## Why DeviceGPT Exists

Modern Android devices generate vast amounts of performance data, but most users lack tools to understand it. DeviceGPT bridges this gap by:

- **Making device data accessible**: Real-time monitoring without root access
- **Enabling power research**: Standardized experiments for camera, display, CPU, and network power consumption
- **Providing actionable insights**: Health scores, recommendations, and AI-powered explanations
- **Supporting research**: CSV export with standardized formats for academic use

See [docs/latest_power_consumption_research.md](docs/latest_power_consumption_research.md) for research methodology and references.

---

## ✨ Features

### 📱 Device Information

- Real-time CPU, RAM, and storage monitoring
- Battery health, temperature, and charging status
- Device model, Android version, and hardware specifications
- Frame rate (FPS) and performance metrics via Choreographer API
- Security status (root detection, developer mode, bootloader state)
- Lock screen widget (Android 13+) for home screen monitoring

**Implementation**: `device_info_ui.kt`, `device_utils.kt`, `SystemMonitorService.kt`, `LockScreenMonitorWidget.kt`

### 🌐 Network Monitoring

- Network type detection (WiFi, Mobile Data, Ethernet)
- IP address (IPv4/IPv6) and connection details
- Real network speed testing (download/upload via HTTP transfers)
- WiFi signal strength (RSSI) and network information
- Network latency measurement (ping-based)

**Implementation**: `network_ui.kt`, `network_utils.kt`, `SystemMonitorService.kt`

### ❤️ Health Tracking

- Device health score calculation based on multiple factors
- Daily streak tracking and history
- Health trends and statistics
- Achievement system for milestones
- Health score recommendations

**Implementation**: `health_section.kt`, `health_score_utils.kt`, `power_achievements.kt`

### ⚡ Power Consumption Analysis

- Component-level power measurement:
  - **Camera**: Per-photo energy measurement with real camera preview
  - **Display**: Brightness curve analysis
  - **CPU**: Micro-benchmark power profiling
  - **Network**: RSSI vs power correlation
- Real-time power consumption tracking via BatteryManager API
- Power consumption history and aggregated statistics
- Power recommendations and alerts
- Educational content about power consumption
- CSV export for research data collection

**Implementation**: `power_consumption_card.kt`, `power_consumption_utils.kt`, `PowerConsumptionAggregator.kt`, `power_recommendations.kt`, `power_alerts.kt`, `power_education.kt`

**Research References**: See [docs/latest_power_consumption_research.md](docs/latest_power_consumption_research.md) for methodology and paper citations.

### 🤖 AI Assistant

- Tab-specific AI prompts for Device, Network, Health, and Power sections
- Simple and Detailed explanation modes
- Context-aware recommendations
- Share device data with AI apps (ChatGPT, Gemini, Claude, DeepSeek, Perplexity, and more)
- Item-specific AI analysis for individual device metrics

**Implementation**: `ai_assistant_dialog.kt`, `ai_prompt_generator.kt`, `robust_ai_sharing.kt`

### 🏆 Additional Features

- **Leaderboard**: Compete on device health metrics with Gmail account linking
- **Device Certificate**: Generate device certificates with resale value estimation via AI
- **System Monitoring Service**: Background foreground service for continuous monitoring
- **Push Notifications**: OneSignal integration for notifications
- **Analytics**: Firebase Analytics with privacy-respecting implementation
- **Material Design 3**: Modern UI with theme support (light/dark mode)
- **Referral System**: Share and track app referrals
- **In-App Review**: Google Play In-App Review API integration

**Implementation**: `LeaderboardSection.kt`, `LeaderboardManager.kt`, `SystemMonitorService.kt`, `Application.kt`, `referral_manager.kt`, `ReviewPromptManager.kt`

---

## 📋 Feature → Code Mapping

Quick reference for developers exploring the codebase:

| Feature | Main Implementation Files |
|---------|--------------------------|
| Device Information | `ui/device_info_ui.kt`, `utils/device_utils.kt` |
| Network Monitoring | `ui/network_ui.kt`, `utils/network_utils.kt` |
| Power Consumption | `ui/power_consumption_card.kt`, `utils/power_consumption_utils.kt` |
| Health Scoring | `ui/health_section.kt`, `utils/health_score_utils.kt` |
| AI Assistant | `ui/ai_assistant_dialog.kt`, `utils/ai_prompt_generator.kt` |
| Background Monitoring | `services/system_monitor_service.kt` |
| Leaderboard | `ui/LeaderboardSection.kt`, `utils/LeaderboardManager.kt` |
| Ad Configuration | `utils/AdConfig.kt` |
| Analytics | `utils/analytics_utils.kt` |

---

## 📸 Screenshots

> **TODO**: Add screenshots to `/docs/images/` directory
> 
> Suggested screenshots:
> - Device Info tab
> - Network Info tab with speed test
> - Health section with score
> - Power Consumption tab with experiments
> - AI Assistant dialog
> - Lock screen widget

---

## 🚀 Quick Start

### Prerequisites

- **Android Studio Iguana (2024.1.1)** or later (required for AGP 8.13.0)
- **JDK 8** or higher
- **Android SDK** (API 24+)
- **Gradle 8.13** (included via wrapper)

### Build and Run

```bash
# Clone the repository
git clone https://github.com/Teamz-Lab-LTD/device-gpt.git
cd device-gpt

# Build debug APK
./gradlew assembleDebug

# Or open in Android Studio and click "Run"
```

The app will run with test AdMob IDs and placeholder configurations. For production features, see [Configuration](#-configuration) below.

---

## ⚙️ Setup

### Configuration

All sensitive configuration is managed via `local_config.properties` (not committed to git). This keeps the repository open-source friendly while allowing you to use your own credentials.

#### 1. Firebase Configuration

**Required for**: Authentication, Firestore, Analytics, Crashlytics, Remote Config

1. Copy the template:
   ```bash
   cp app/google-services.json.template app/google-services.json
   ```

2. Get your Firebase config from [Firebase Console](https://console.firebase.google.com/)

3. Replace all placeholder values in `app/google-services.json`:
   - `YOUR_PROJECT_NUMBER`
   - `YOUR_PROJECT_ID`
   - `YOUR_MOBILE_SDK_APP_ID`
   - `YOUR_OAUTH_CLIENT_ID`
   - `YOUR_FIREBASE_API_KEY`
   - `YOUR_ADMOB_APP_ID`

#### 2. AdMob Configuration (Optional)

**Required for**: Displaying ads

1. Copy the template:
   ```bash
   cp local_config.template local_config.properties
   ```

2. Add your AdMob IDs to `local_config.properties`:
   ```properties
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
   APP_OPEN_AD_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   INTERSTITIAL_AD_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   NATIVE_AD_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   REWARDED_AD_UNIT_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   ```

3. The app will automatically use these IDs via `AdConfig.kt`. No manual file editing needed.

**Note**: If `local_config.properties` is missing, the app uses Google test ad IDs (debug builds only).

#### 3. OAuth Client ID Configuration (Optional)

**Required for**: Google Sign-In (leaderboard feature)

Add to `local_config.properties`:
```properties
OAUTH_CLIENT_ID=YOUR_CLIENT_ID.apps.googleusercontent.com
```

The OAuth Client ID is automatically injected into `strings.xml` at build time. No manual editing needed.

#### 4. OneSignal Configuration (Optional)

**Required for**: Push notifications

Add to `local_config.properties`:
```properties
ONESIGNAL_APP_ID=your-onesignal-app-id
```

#### 5. Signing Configuration (Optional - for release builds)

1. Copy the template:
   ```bash
   cp key.properties.template key.properties
   ```

2. Generate a keystore:
   ```bash
   keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
   ```

3. Update `key.properties` with your credentials:
   ```properties
   storePassword=your_store_password
   keyPassword=your_key_password
   keyAlias=release-key
   storeFile=release-key.jks
   ```

**Note**: For debug builds, this step is optional. The app will use debug signing if `key.properties` is missing.

### Configuration Files Summary

| File | Purpose | Required | Template |
|------|---------|----------|----------|
| `app/google-services.json` | Firebase configuration | Yes | `app/google-services.json.template` |
| `local_config.properties` | AdMob, OAuth, OneSignal IDs | Optional | `local_config.template` |
| `key.properties` | Release signing | Optional (release only) | `key.properties.template` |
| `local.properties` | Android SDK path | Auto-generated | N/A |

**⚠️ Important**: Never commit sensitive files. They are already in `.gitignore`.

---

## 🔧 Troubleshooting

### Common Setup Issues

#### `google-services.json` Missing or Invalid

**Symptoms**: Build fails with "File google-services.json is missing" or Firebase initialization errors.

**Solution**:
1. Ensure `app/google-services.json` exists (copy from template)
2. Verify all placeholder values are replaced with actual Firebase credentials
3. Check that `package_name` in `google-services.json` matches `applicationId` in `app/build.gradle.kts` (should be `com.teamz.lab.debugger`)
4. Sync project: `File → Sync Project with Gradle Files` in Android Studio

#### AdMob IDs Showing Placeholders

**Symptoms**: App shows test ads or "YOUR_ADMOB_APP_ID" in logs.

**Solution**:
1. Create `local_config.properties` from `local_config.template`
2. Add your AdMob App ID and Ad Unit IDs to `local_config.properties`
3. Ensure file is in project root (same level as `build.gradle.kts`)
4. Rebuild project: `./gradlew clean assembleDebug`
5. Verify `AdConfig.kt` reads from `BuildConfig` fields (set at build time)

#### OAuth Client ID Mismatch

**Symptoms**: Google Sign-In fails with "OAuth client ID mismatch" error.

**Solution**:
1. Verify OAuth Client ID in `local_config.properties` matches Firebase Console
2. Ensure format is correct: `YOUR_CLIENT_ID.apps.googleusercontent.com` (include `.apps.googleusercontent.com` suffix)
3. Check that OAuth Client ID in Firebase Console is for package name `com.teamz.lab.debugger`
4. Rebuild project to inject ID into `strings.xml` via `resValue` in `build.gradle.kts`

#### Gradle Sync or Version Issues

**Symptoms**: "Gradle sync failed" or "Unsupported class file major version" errors.

**Solution**:
1. **Android Studio Version**: Ensure Android Studio Iguana (2024.1.1) or later
2. **JDK Version**: Use JDK 8 or higher (check: `File → Project Structure → SDK Location → JDK location`)
3. **Gradle Wrapper**: Use included wrapper: `./gradlew --version` should show Gradle 8.13
4. **Clean Build**: 
   ```bash
   ./gradlew clean
   ./gradlew --stop
   ```
   Then sync again in Android Studio
5. **Invalidate Caches**: `File → Invalidate Caches → Invalidate and Restart`

#### Build Fails with "Cannot find symbol" or Missing Dependencies

**Solution**:
1. Sync Gradle: `File → Sync Project with Gradle Files`
2. Check internet connection (Gradle downloads dependencies)
3. Clear Gradle cache: `rm -rf ~/.gradle/caches/` (macOS/Linux) or `%USERPROFILE%\.gradle\caches\` (Windows)
4. Rebuild: `./gradlew clean build`

---

## 🏗️ Architecture

### High-Level Overview

- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose with Material Design 3
- **Language**: Kotlin 2.1.0
- **Dependency Injection**: Manual (no Hilt/Koin)

### Key Components

- **MainActivity**: Single-activity app with tab-based navigation
- **ViewModels**: `DeviceInfoViewModel`, `PowerConsumptionViewModel` for state management
- **Background Service**: `SystemMonitorService` (foreground service) for continuous monitoring
- **Data Flow**: 
  - UI → ViewModel → Utils/Services
  - Services → SharedPreferences/Flow → UI
- **Firebase Integration**: Auth, Firestore, Analytics, Crashlytics, Remote Config

### Project Structure

```
device-gpt/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/teamz/lab/debugger/
│   │   │   │   ├── MainActivity.kt          # Main activity
│   │   │   │   ├── Application.kt          # App initialization
│   │   │   │   ├── ui/                      # UI components
│   │   │   │   │   ├── device_info_ui.kt
│   │   │   │   │   ├── network_ui.kt
│   │   │   │   │   ├── health_section.kt
│   │   │   │   │   ├── power_consumption_card.kt
│   │   │   │   │   ├── LeaderboardSection.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── utils/                   # Utility classes
│   │   │   │   │   ├── AdConfig.kt          # Centralized ad config
│   │   │   │   │   ├── power_consumption_utils.kt
│   │   │   │   │   ├── device_utils.kt
│   │   │   │   │   ├── LeaderboardManager.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── services/                # Background services
│   │   │   │   │   └── system_monitor_service.kt
│   │   │   │   ├── widgets/                 # App widgets
│   │   │   │   │   └── LockScreenMonitorWidget.kt
│   │   │   │   └── receivers/               # Broadcast receivers
│   │   │   └── res/                         # Resources
│   │   ├── test/                            # Unit tests (31 files)
│   │   └── androidTest/                     # UI tests (17 files)
│   ├── build.gradle.kts
│   └── google-services.json.template
├── docs/                                    # Documentation
│   ├── latest_power_consumption_research.md
│   └── Bridging the Gap Between Research Papers and Code.pdf
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── LICENSE
└── CONTRIBUTING.md
```

---

## 🧪 Testing

The project includes comprehensive test coverage:

- **Unit Tests**: 31 test files covering utilities, managers, and core functionality
- **Android Tests**: 17 test files for UI components, user flows, and integration testing

### Running Tests

```bash
# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run all UI tests (requires device/emulator)
./gradlew :app:connectedAndroidTest

# Generate coverage report
./gradlew :app:testDebugUnitTest
./gradlew :app:jacocoTestReport
```

Coverage reports are generated in `app/build/reports/jacoco/jacocoTestReport/html/index.html`

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed testing information.

---

## 🔒 Privacy & Data

### Data Collection

**Local Data (Stored on Device):**
- Device information (CPU, RAM, battery, network stats)
- Health scores and streaks
- Power consumption measurements
- App preferences and settings

**Remote Data (Firebase):**
- **Firebase Analytics**: App usage events, feature interactions (anonymized)
- **Firebase Crashlytics**: Crash reports and stack traces
- **Firebase Firestore**: Leaderboard data (health scores, user IDs)
- **Firebase Auth**: Anonymous authentication and optional Gmail linking
- **Firebase Remote Config**: Feature flags and ad configuration

**OneSignal:**
- Push notification tokens and delivery status

### Privacy Features

- **Anonymous Authentication**: Leaderboard uses Firebase anonymous auth by default
- **Optional Gmail Linking**: Users can optionally link Gmail for leaderboard persistence
- **Analytics Respects Device Settings**: Analytics are not sent when device is in:
  - Battery Saver Mode
  - Do Not Disturb Mode
  - Airplane Mode
  - Doze Mode (deep sleep)

### How to Disable Analytics

Analytics are automatically disabled in restricted device modes (see above). For complete opt-out:

1. Disable Firebase Analytics in your Firebase project console
2. Or modify `AnalyticsUtils.kt` to always return early in `logEvent()`

**Note**: Analytics help improve the app. Consider keeping them enabled to support development.

---

## 🛠️ Tech Stack

- **Language**: Kotlin 2.1.0
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with ViewModel
- **Dependency Injection**: Manual (can be migrated to Hilt/Koin)
- **Backend**: 
  - Firebase (Firestore, Analytics, Crashlytics, Remote Config, Auth)
  - OneSignal (Push Notifications)
- **Ads**: Google AdMob
- **Background Tasks**: WorkManager
- **Authentication**: Firebase Auth + Credential Manager API
- **Testing**: JUnit, Robolectric, Espresso, Compose UI Test
- **Build System**: Gradle 8.13 with Kotlin DSL

---

## 🗺️ Roadmap

Future improvements (based on code TODOs and research docs):

- **Enhanced Power Experiments**: Display brightness curve calibration, CPU micro-benchmark improvements
- **ODPM Integration**: On-Device Power Rails Monitor support where available
- **AI Workload Monitoring**: Power analysis for AI/ML inference tasks
- **Comparative Analysis**: Device-to-device power consumption comparisons
- **Research Data Portal**: Web interface for aggregated power research data

See [docs/latest_power_consumption_research.md](docs/latest_power_consumption_research.md) for detailed research roadmap.

---

## ❓ FAQ

### Does it require root access?

**No.** DeviceGPT works on stock Android devices without root access. All monitoring uses standard Android APIs:
- BatteryManager API for power measurements
- ActivityManager for RAM/CPU info
- Network APIs for network testing
- System APIs for device information

Some advanced features (like detailed CPU frequency monitoring) may have limited data on non-root devices, but core functionality works without root.

### Does it work offline?

**Partially.** Most features work offline:
- ✅ Device information (CPU, RAM, battery, storage)
- ✅ Health scoring and history
- ✅ Power consumption tracking
- ✅ Local data viewing

Features that require internet:
- ❌ Network speed testing (download/upload)
- ❌ Network latency measurement
- ❌ Leaderboard sync
- ❌ AI Assistant sharing
- ❌ Firebase Analytics/Crashlytics

### Can I export data (CSV)?

**Yes.** Power consumption experiments support CSV export:
- Camera power experiments
- CPU micro-benchmark tests
- App power consumption data
- Network RSSI vs power correlation

CSV files are exported via `PowerConsumptionUtils.exportExperimentCSV()` and can be shared through Android's share dialog. See `power_consumption_card.kt` for export UI implementation.

### What device versions are supported?

**Android 7.0 (API 24) and higher.**
- **Minimum SDK**: API 24 (Android 7.0 Nougat)
- **Target SDK**: API 36 (Android 15)
- **Compile SDK**: API 36

Some features have additional requirements:
- Lock screen widget: Android 13+ (API 33+)
- Notification permission: Android 13+ (API 33+)
- Background location: Android 10+ (API 29+) for some network features

### How is power measured?

Power measurement uses the **BatteryManager API** with the physics formula **P = V × I**:
- **Voltage (V)**: From `BatteryManager.EXTRA_VOLTAGE` (real millivolts)
- **Current (I)**: From `BatteryManager.BATTERY_PROPERTY_CURRENT_NOW` (real microamps)

**Methodology** (from `power_consumption_utils.kt`):
- Real system data only (no estimates or simulations)
- Component-level measurements: Camera, Display, CPU, Network
- If real data unavailable, returns 0.0 (no fallback estimates)
- Uses baseline → workload → post-workload delta measurements

For detailed methodology and research references, see [docs/latest_power_consumption_research.md](docs/latest_power_consumption_research.md).

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

### Quick Start for Contributors

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Write/update tests (unit tests for utils, UI tests for composables)
5. Ensure all tests pass: `./gradlew :app:testDebugUnitTest`
6. Commit your changes (`git commit -m 'Add amazing feature'`)
7. Push to the branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Write tests for new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Research papers and methodologies that inspired the power consumption features (see [docs/latest_power_consumption_research.md](docs/latest_power_consumption_research.md))
- Android community for excellent tools and libraries
- All contributors who help improve this project

---

## 📞 Support

**App Support:**
- 📱 [Google Play Store](https://play.google.com/store/apps/details?id=com.teamz.lab.debugger) - Rate, review, and get app updates
- 📧 **Email**: hello@teamzlab.com
- 📞 **Phone**: +44 7365 602184

**Development Support:**
- **Issues**: [GitHub Issues](https://github.com/Teamz-Lab-LTD/device-gpt/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Teamz-Lab-LTD/device-gpt/discussions)
- **Community**: [XDA Forums](https://xdaforums.com/t/app-devicegpt-ai-powered-android-monitor-real-data-privacy-guardian-global-leaderboard.4773593/)

---

## ⭐ Show Your Support

If you find this project useful:

1. **⭐ Star this repository** on GitHub
2. **📱 [Download DeviceGPT](https://play.google.com/store/apps/details?id=com.teamz.lab.debugger)** on Google Play
3. **⭐ Rate and review** on Google Play Store
4. **🚀 [Upvote on Product Hunt](https://www.producthunt.com/p/devicegpt-ai-phone-health)**
5. **💬 [Join the discussion](https://xdaforums.com/t/app-devicegpt-ai-powered-android-monitor-real-data-privacy-guardian-global-leaderboard.4773593/)** on XDA Forums

Your support helps us continue building great open-source tools!

---

## 🏢 Built by Teamz Lab — Need a Similar App?

<div align="center">

**DeviceGPT is a production app with 500+ downloads on Google Play**

[![Download on Google Play](https://img.shields.io/badge/Download%20DeviceGPT-Google%20Play-4285F4?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=com.teamz.lab.debugger)

**See it in action → [View on Google Play](https://play.google.com/store/apps/details?id=com.teamz.lab.debugger)**

</div>

DeviceGPT was built by **Teamz Lab**, a remote-first, design-first mobile development agency specializing in AI-powered applications and comprehensive quality assurance.

**This is a real, production app** — not a demo. We build apps that users actually download and use.

### Why Work with Teamz Lab?

- **AI-Powered Development**: We leverage AI tools and methodologies to accelerate development while maintaining quality (see DeviceGPT's AI integration)
- **Design-First Approach**: User experience and visual design are core to our process (Material Design 3, modern UI)
- **Comprehensive SQA**: Rigorous testing and quality assurance at every stage (31 unit tests, 17 UI tests)
- **Remote-First Team**: Global talent, seamless collaboration
- **Full-Stack Mobile**: Native Android, iOS, and cross-platform solutions
- **Research-Backed**: We implement cutting-edge research findings into production apps (see power consumption research)
- **Proven Track Record**: Multiple successful apps on Google Play and App Store
- **Production-Ready Code**: Clean, maintainable, open-source friendly architecture

### Our Portfolio & Credentials

**Website**: https://teamzlab.com/

**Upwork Agency**: https://www.upwork.com/agencies/1904602719490921565/

**Play Store Portfolio**: https://play.google.com/store/apps/dev?id=7194763656319643086

**App Store Portfolio**: https://apps.apple.com/us/developer/teamz-lab-ltd/id1785282466

**Clutch Profile**: https://clutch.co/profile/teamz-lab

**Trustpilot Reviews**: https://uk.trustpilot.com/review/teamzlab.com

**LinkedIn**: https://www.linkedin.com/company/teamzlab/posts/?feedView=all

**Twitter/X**: https://x.com/teamzlabapp

**Instagram**: https://www.instagram.com/teamzlab/

**YouTube**: https://www.youtube.com/@teamzlab

### See Our Work in Action

**DeviceGPT** is available now:
- 📱 [Download on Google Play](https://play.google.com/store/apps/details?id=com.teamz.lab.debugger) (500+ downloads)
- 🚀 [Upvote on Product Hunt](https://www.producthunt.com/p/devicegpt-ai-phone-health)
- 💬 [Discuss on XDA Forums](https://xdaforums.com/t/app-devicegpt-ai-powered-android-monitor-real-data-privacy-guardian-global-leaderboard.4773593/)

### Let's Build Something Great

**Need a similar app or custom mobile solution?**

We've built DeviceGPT from scratch — architecture, AI integration, power research, analytics, and more. We can build your app too.

**Get in touch:**
- 📧 **Email**: hello@teamzlab.com
- 📞 **Phone**: +44 7365 602184
- 🌐 **Website**: https://teamzlab.com/
- 💼 **Upwork Agency**: https://www.upwork.com/agencies/1904602719490921565/

**What we offer:**
- Custom Android/iOS app development
- AI-powered features integration
- Research-backed implementations
- Production-ready code architecture
- Comprehensive testing and QA
- Design-first UI/UX

---

<div align="center">

**Made with ❤️ by [Teamz Lab](https://teamzlab.com/)**

[⬆ Back to Top](#devicegpt-)

</div>

---

## 📝 Credibility & Accuracy

We strive for accuracy in this README. If you find any mismatch between the documentation and the actual codebase:

1. **Open an issue** with:
   - File path(s) where you found the discrepancy
   - Screenshot or code snippet showing the actual behavior
   - Expected behavior based on README

2. **Submit a PR** if you can fix it:
   - Update the README with accurate information
   - Reference the code files that verify your changes
   - Follow the [Contributing guidelines](#-contributing)

We welcome contributions that improve documentation accuracy and developer experience.

---

## Change Log (README)

### Removed/Changed for Accuracy

1. **Version Badges**: Updated Kotlin from 1.9 to 2.1, Compose badge to reflect Material 3
2. **Setup Instructions (Steps 4-5)**: Removed outdated manual file editing instructions. Now correctly uses `local_config.properties` approach via `AdConfig.kt`
3. **"100% Real Data" Claims**: Softened to "real-time monitoring" and "real network speed testing" where verified, removed unverified absolute claims
4. **"Research-Grade" Claims**: Changed to "Component-level power measurement" with reference to research docs, only claims "research-grade" where docs cite papers
5. **Test Counts**: Updated from "25+" to "31" unit tests, "7+" to "17" Android tests
6. **Prerequisites**: Updated Android Studio requirement from Hedgehog (2023.1.1) to Iguana (2024.1.1) for AGP 8.13.0 compatibility
7. **Configuration Files**: Added `local_config.properties` to configuration section (was missing)
8. **Project Structure**: Added `widgets/` and `receivers/` directories that were missing
9. **Features**: Added Lock screen widget, OneSignal, referral system, in-app review that were missing
10. **Tech Stack**: Added OneSignal, WorkManager, Credential Manager API that were missing
11. **Privacy Section**: Added comprehensive privacy and data collection section with Firebase products verification
12. **Teamz Lab CTA**: Added complete lead generation section with all requested links and benefits
13. **Architecture Section**: Added high-level architecture overview with data flow
14. **Screenshots Section**: Added placeholder with TODO for future screenshots

### Enhanced (Latest Update)

15. **Feature → Code Mapping**: Added quick reference table mapping features to implementation files
16. **Troubleshooting Section**: Added comprehensive troubleshooting guide for common setup issues (google-services.json, AdMob IDs, OAuth, Gradle)
17. **FAQ Section**: Added 5 frequently asked questions (root access, offline support, CSV export, device versions, power measurement methodology)
18. **Credibility Footer**: Added section encouraging issue reports and PRs for documentation accuracy
19. **Download Section**: Added prominent download section with Google Play Store, Product Hunt, and XDA Forums links
20. **Lead Generation Optimization**: Enhanced Teamz Lab CTA section with production app proof, contact information, and multiple call-to-action points
21. **App Links Integration**: Added app download links throughout README (header, features, support, Teamz Lab section)

### Preserved

- All working Gradle commands
- Correct file paths and project structure
- Accurate feature descriptions verified in code
- Research documentation references
- Contributing guidelines structure
