# Setup Guide for DeviceGPT

This guide provides detailed instructions for setting up DeviceGPT for development.

## 📋 Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 8 or higher
- **Android SDK**: API 24 (Android 7.0) or higher
- **Gradle**: 8.0+ (included in project)

## 🔧 Step-by-Step Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/debugger.git
cd debugger
```

### 2. Configure Firebase

Firebase is required for:
- Analytics
- Crashlytics
- Remote Config
- Firestore (Leaderboard)
- Authentication

**Steps:**

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app with package name: `com.teamz.lab.debugger`
4. Download `google-services.json`
5. Copy the template:
   ```bash
   cp app/google-services.json.template app/google-services.json
   ```
6. Replace all placeholder values in `google-services.json` with your Firebase credentials:
   - `YOUR_PROJECT_NUMBER` → Your Firebase project number
   - `YOUR_PROJECT_ID` → Your Firebase project ID
   - `YOUR_MOBILE_SDK_APP_ID` → Your mobile SDK app ID
   - `YOUR_OAUTH_CLIENT_ID` → Your OAuth client ID
   - `YOUR_FIREBASE_API_KEY` → Your Firebase API key
   - `YOUR_ADMOB_APP_ID` → Your AdMob app ID (optional)

### 3. Configure AdMob (Optional)

If you want to use ads in your build:

1. Go to [AdMob Console](https://apps.admob.com/)
2. Create an app and get your App ID
3. Create ad units for:
   - App Open Ads
   - Interstitial Ads
   - Rewarded Ads
   - Native Ads

4. Replace AdMob IDs in the following files:

   **AndroidManifest.xml:**
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="YOUR_ADMOB_APP_ID" />
   ```

   **app_open_manager.kt:**
   ```kotlin
   private val AD_UNIT_ID = if (BuildConfig.DEBUG) {
       "ca-app-pub-3940256099942555/9257395921" // Test ID
   } else {
       "YOUR_PRODUCTION_APP_OPEN_AD_UNIT_ID"
   }
   ```

   **interstitial_ad_manager.kt:**
   ```kotlin
   private val AD_UNIT_ID = if (BuildConfig.DEBUG) {
       "ca-app-pub-3940256099942544/1033173712" // Test ID
   } else {
       "YOUR_PRODUCTION_INTERSTITIAL_AD_UNIT_ID"
   }
   ```

   **rewarded_ad_manager.kt:**
   ```kotlin
   private val AD_UNIT_ID = if (BuildConfig.DEBUG) {
       "ca-app-pub-3940256099942544/5224354917" // Test ID
   } else {
       "YOUR_PRODUCTION_REWARDED_AD_UNIT_ID"
   }
   ```

   **expandable_info_list.kt:**
   ```kotlin
   private val AD_UNIT_ID = if (BuildConfig.DEBUG) {
       "ca-app-pub-3940256099942544/2247696110" // Test ID
   } else {
       "YOUR_PRODUCTION_NATIVE_AD_UNIT_ID"
   }
   ```

### 4. Configure OAuth Client ID (Optional)

For Gmail sign-in and leaderboard features:

1. In Firebase Console, go to Authentication → Sign-in method → Google
2. Enable Google sign-in
3. Get your Web client ID
4. Update `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
   ```

### 5. Configure Signing (For Release Builds Only)

**For Debug Builds:** This step is optional. Debug builds will use the default debug keystore.

**For Release Builds:**

1. Generate a keystore:
   ```bash
   keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
   ```

2. Copy the template:
   ```bash
   cp key.properties.template key.properties
   ```

3. Update `key.properties`:
   ```properties
   storePassword=YOUR_STORE_PASSWORD
   keyPassword=YOUR_KEY_PASSWORD
   keyAlias=release-key
   storeFile=release-key.jks
   ```

4. Place `release-key.jks` in the root directory

**⚠️ Important:** Never commit `key.properties` or `release-key.jks` to version control!

### 6. Configure Local Properties

Android Studio will automatically create `local.properties` with your SDK path. If it doesn't exist:

1. Create `local.properties` in the root directory
2. Add:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   ```

### 7. Build and Run

**Using Gradle:**
```bash
./gradlew assembleDebug
./gradlew installDebug
```

**Using Android Studio:**
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Click "Run" or press `Shift+F10`

## 🧪 Verify Setup

### Run Tests

```bash
# Unit tests (no device needed)
./gradlew :app:testDebugUnitTest

# UI tests (requires device/emulator)
./gradlew :app:connectedAndroidTest
```

### Check Build

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

## 🔍 Troubleshooting

### Firebase Issues

**Problem:** `google-services.json not found`
- **Solution:** Make sure you've copied the template and filled in your Firebase credentials

**Problem:** Firebase initialization errors
- **Solution:** Verify all values in `google-services.json` are correct
- Check that your package name matches: `com.teamz.lab.debugger`

### Build Issues

**Problem:** `key.properties not found` (Release builds)
- **Solution:** This is expected for debug builds. For release builds, create `key.properties` following step 5.

**Problem:** Gradle sync fails
- **Solution:** 
  - Check your internet connection (Gradle needs to download dependencies)
  - Invalidate caches: File → Invalidate Caches / Restart
  - Check `gradle/wrapper/gradle-wrapper.properties` for correct Gradle version

**Problem:** SDK not found
- **Solution:** 
  - Check `local.properties` has correct SDK path
  - In Android Studio: File → Project Structure → SDK Location

### AdMob Issues

**Problem:** Ads not showing
- **Solution:** 
  - Verify AdMob IDs are correct
  - Check that you're using test IDs in debug mode
  - Ensure your AdMob account is set up correctly
  - Check device logs for AdMob errors

### Permission Issues

**Problem:** App crashes when requesting permissions
- **Solution:** 
  - Check that all required permissions are in `AndroidManifest.xml`
  - Test on a device with Android 6.0+ (runtime permissions)

## 📱 Testing on Device

1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times

2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging

3. Connect device and run:
   ```bash
   ./gradlew installDebug
   ```

## 🔐 Security Notes

- **Never commit sensitive files:**
  - `key.properties`
  - `google-services.json`
  - `release-key.jks`
  - `local.properties`

- These files are already in `.gitignore`

- **For production:**
  - Use different Firebase projects for development and production
  - Use different AdMob accounts if needed
  - Keep your keystore secure and backed up

## 📚 Next Steps

- Read [README.md](README.md) for project overview
- Check [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines
- Review [TESTING_GUIDE.md](TESTING_GUIDE.md) for testing information

## ❓ Need Help?

- Check existing [GitHub Issues](https://github.com/yourusername/debugger/issues)
- Open a [GitHub Discussion](https://github.com/yourusername/debugger/discussions)
- Review the codebase and documentation

---

**Happy Coding! 🚀**

