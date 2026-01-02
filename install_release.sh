#!/bin/bash

# Script to install release APK on connected Android device
# Usage: ./install_release.sh

APK_PATH="app/build/outputs/apk/release/app-release.apk"

echo "🔍 Checking for connected devices..."
DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq "0" ]; then
    echo "❌ No devices connected!"
    echo ""
    echo "Please:"
    echo "1. Connect your Google Pixel 8a via USB"
    echo "2. Enable USB Debugging (Settings → Developer Options)"
    echo "3. Authorize this computer on your device"
    echo "4. Run 'adb devices' to verify connection"
    exit 1
fi

echo "✅ Device(s) connected: $DEVICES"
adb devices

echo ""
echo "📦 Installing release APK..."
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK not found at $APK_PATH"
    echo "Building release APK..."
    ./gradlew assembleRelease
fi

echo "Installing: $APK_PATH"
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Installation successful!"
    echo ""
    echo "🚀 Launching app..."
    adb shell am start -n com.teamz.lab.debugger/.MainActivity
    
    echo ""
    echo "📊 Monitoring logs (Ctrl+C to stop)..."
    echo "Filtering: AppOpenAdManager, ImprovedAdManager, MyApplication"
    adb logcat -c
    adb logcat | grep -E "AppOpenAdManager|ImprovedAdManager|MyApplication|RemoteConfigUtils"
else
    echo "❌ Installation failed!"
    exit 1
fi

