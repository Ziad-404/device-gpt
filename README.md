# DeviceGPT 📱

<div align="center">

![DeviceGPT](https://img.shields.io/badge/DeviceGPT-Android-blue?style=for-the-badge&logo=android)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue?style=for-the-badge&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5-orange?style=for-the-badge)

**A comprehensive Android device monitoring and power consumption analysis app**

[Features](#-features) • [Setup](#-setup) • [Contributing](#-contributing) • [License](#-license)

</div>

---

## 📖 About

DeviceGPT is an advanced Android application that provides comprehensive device monitoring, health analysis, and power consumption research capabilities. Built with modern Android development practices, it offers real-time insights into your device's performance, network status, health metrics, and power consumption patterns.

### Key Highlights

- 🔍 **Real Device Data**: 100% real device data - no estimates or simulations
- 📊 **Power Consumption Research**: Implements cutting-edge power measurement techniques
- 🤖 **AI-Powered Insights**: Get intelligent explanations and recommendations
- 📈 **Health Tracking**: Monitor device health with scores, streaks, and history
- 🌐 **Network Analysis**: Comprehensive network information and speed testing
- 🏆 **Leaderboard**: Compete with others on device health metrics

---

## ✨ Features

### 📱 Device Information
- Real-time CPU, RAM, and storage monitoring
- Battery health and temperature tracking
- Device model, Android version, and hardware details
- Frame rate and performance metrics
- Security status (root detection, developer mode)

### 🌐 Network Monitoring
- Network type detection (WiFi, Mobile Data, etc.)
- IP address and connection details
- Real network speed testing (download/upload)
- WiFi signal strength and network information
- Network latency measurement

### ❤️ Health Tracking
- Device health score calculation
- Daily streak tracking
- Health history and trends
- Improvement suggestions
- Achievement system

### ⚡ Power Consumption Analysis
- Component-level power measurement (Camera, Display, CPU, Network)
- Real-time power consumption tracking
- Power consumption history and statistics
- Power recommendations and alerts
- Educational content about power consumption
- Research-grade power measurement experiments

### 🤖 AI Assistant
- Tab-specific AI prompts for Device, Network, Health, and Power
- Simple and Detailed explanation modes
- Context-aware recommendations
- Share device data with AI for analysis

### 🏆 Additional Features
- Leaderboard system with Gmail integration
- Device certificate generation
- Real-time system monitoring service
- Comprehensive analytics
- Material Design 3 UI with theme support
- Dark mode support

---

## 🚀 Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or higher
- Android SDK (API 24+)
- Gradle 8.0+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/debugger.git
   cd debugger
   ```

2. **Configure Firebase**
   - Copy `app/google-services.json.template` to `app/google-services.json`
   - Replace all placeholder values with your Firebase project credentials
   - Get your Firebase config from [Firebase Console](https://console.firebase.google.com/)

3. **Configure Signing (Optional - for release builds)**
   - Copy `key.properties.template` to `key.properties` (in root directory)
   - Generate a keystore:
     ```bash
     keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
     ```
   - Update `key.properties` with your credentials
   - **Note**: For debug builds, this step is optional

4. **Configure AdMob (Optional)**
   - Replace AdMob App ID in `app/src/main/AndroidManifest.xml`
   - Replace Ad Unit IDs in:
     - `app/src/main/java/com/teamz/lab/debugger/utils/app_open_manager.kt`
     - `app/src/main/java/com/teamz/lab/debugger/utils/interstitial_ad_manager.kt`
     - `app/src/main/java/com/teamz/lab/debugger/utils/rewarded_ad_manager.kt`
     - `app/src/main/java/com/teamz/lab/debugger/ui/expandable_info_list.kt`

5. **Configure OAuth Client ID (Optional)**
   - Replace the `default_web_client_id` in `app/src/main/res/values/strings.xml` with your OAuth client ID

6. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open the project in Android Studio and click "Run"

---

## 🏗️ Project Structure

```
debugger/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/teamz/lab/debugger/
│   │   │   │   ├── MainActivity.kt          # Main activity
│   │   │   │   ├── ui/                      # UI components
│   │   │   │   │   ├── power_consumption_card.kt
│   │   │   │   │   ├── expandable_info_list.kt
│   │   │   │   │   └── ...
│   │   │   │   ├── utils/                   # Utility classes
│   │   │   │   │   ├── ai_prompt_generator.kt
│   │   │   │   │   ├── power_consumption_utils.kt
│   │   │   │   │   ├── device_utils.kt
│   │   │   │   │   └── ...
│   │   │   │   └── services/                # Background services
│   │   │   │       └── system_monitor_service.kt
│   │   │   └── res/                         # Resources
│   │   ├── test/                            # Unit tests
│   │   └── androidTest/                     # UI tests
│   ├── build.gradle.kts
│   └── google-services.json.template
├── docs/                                    # Documentation
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

- **Unit Tests**: 25+ test files covering all utilities and core functionality
- **UI Tests**: 7+ test files for UI components and user flows
- **Integration Tests**: Complete user flow testing

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

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed testing information.

---

## 🔬 Research & Power Consumption

DeviceGPT implements cutting-edge power consumption measurement techniques based on recent research papers. The app provides:

- **Standardized Testing Protocol**: Unified power testing across all major components
- **Component-Level Power Data**: Camera, Display, CPU, and Network power measurements
- **Research Data Collection**: CSV export with standardized format
- **Reproducible Experiments**: Works on any Android device without root access

See [docs/latest_power_consumption_research.md](docs/latest_power_consumption_research.md) for detailed research documentation.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModel
- **Dependency Injection**: Manual (can be migrated to Hilt/Koin)
- **Backend**: Firebase (Firestore, Analytics, Crashlytics, Remote Config, Auth)
- **Ads**: Google AdMob
- **Testing**: JUnit, Robolectric, Espresso, Compose UI Test
- **Build System**: Gradle with Kotlin DSL

---

## 📝 Configuration Files

### Required for Development

- `app/google-services.json` - Firebase configuration (use template)
- `local.properties` - Android SDK path (auto-generated)

### Optional for Release Builds

- `key.properties` - Signing configuration (use template)
- `release-key.jks` - Keystore file (generate yourself)

**⚠️ Important**: Never commit sensitive files. They are already in `.gitignore`.

---

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Quick Start for Contributors

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Write/update tests
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Research papers and methodologies that inspired the power consumption features
- Android community for excellent tools and libraries
- All contributors who help improve this project

---

## 📞 Contact & Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/debugger/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/debugger/discussions)

---

## ⭐ Show Your Support

If you find this project useful, please consider giving it a star ⭐ on GitHub!

---

<div align="center">

**Made with ❤️ by Teamz Lab**

[⬆ Back to Top](#devicegpt-)

</div>

