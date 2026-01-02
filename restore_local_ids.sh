#!/bin/bash
# Script to restore your local IDs from local_config.properties
# This allows you to use your actual IDs locally while keeping the repo open source friendly

set -e

CONFIG_FILE="local_config.properties"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "❌ Error: $CONFIG_FILE not found!"
    echo ""
    echo "Please create $CONFIG_FILE by copying local_config.template:"
    echo "  cp local_config.template local_config.properties"
    echo ""
    echo "Then fill in your actual IDs in local_config.properties"
    exit 1
fi

echo "📝 Reading configuration from $CONFIG_FILE..."
source "$CONFIG_FILE"

# Check if all required variables are set
if [ -z "$ADMOB_APP_ID" ] || [ -z "$APP_OPEN_AD_UNIT_ID" ] || [ -z "$OAUTH_CLIENT_ID" ]; then
    echo "❌ Error: Some required IDs are missing in $CONFIG_FILE"
    exit 1
fi

echo "✅ Restoring IDs to source files..."
echo ""

# Restore AdMob App ID in AndroidManifest.xml
if [ -f "app/src/main/AndroidManifest.xml" ]; then
    sed -i '' "s/YOUR_ADMOB_APP_ID/$ADMOB_APP_ID/g" "app/src/main/AndroidManifest.xml"
    echo "  ✅ Restored AdMob App ID in AndroidManifest.xml"
fi

# Restore OAuth Client ID in strings.xml
if [ -f "app/src/main/res/values/strings.xml" ]; then
    sed -i '' "s/YOUR_OAUTH_CLIENT_ID/$OAUTH_CLIENT_ID/g" "app/src/main/res/values/strings.xml"
    echo "  ✅ Restored OAuth Client ID in strings.xml"
fi

# Restore App Open Ad Unit ID (use | as delimiter to avoid issues with /)
if [ -f "app/src/main/java/com/teamz/lab/debugger/utils/app_open_manager.kt" ]; then
    sed -i '' "s|YOUR_APP_OPEN_AD_UNIT_ID|$APP_OPEN_AD_UNIT_ID|g" "app/src/main/java/com/teamz/lab/debugger/utils/app_open_manager.kt"
    echo "  ✅ Restored App Open Ad Unit ID"
fi

# Restore Interstitial Ad Unit ID
if [ -f "app/src/main/java/com/teamz/lab/debugger/utils/interstitial_ad_manager.kt" ]; then
    sed -i '' "s|YOUR_INTERSTITIAL_AD_UNIT_ID|$INTERSTITIAL_AD_UNIT_ID|g" "app/src/main/java/com/teamz/lab/debugger/utils/interstitial_ad_manager.kt"
    echo "  ✅ Restored Interstitial Ad Unit ID"
fi

# Restore Native Ad Unit ID
if [ -f "app/src/main/java/com/teamz/lab/debugger/ui/expandable_info_list.kt" ]; then
    sed -i '' "s|YOUR_NATIVE_AD_UNIT_ID|$NATIVE_AD_UNIT_ID|g" "app/src/main/java/com/teamz/lab/debugger/ui/expandable_info_list.kt"
    echo "  ✅ Restored Native Ad Unit ID"
fi

# Restore Rewarded Ad Unit ID (if set)
if [ -n "$REWARDED_AD_UNIT_ID" ] && [ "$REWARDED_AD_UNIT_ID" != "YOUR_REWARDED_AD_UNIT_ID" ]; then
    if [ -f "app/src/main/java/com/teamz/lab/debugger/utils/rewarded_ad_manager.kt" ]; then
        sed -i '' "s|YOUR_REWARDED_AD_UNIT_ID|$REWARDED_AD_UNIT_ID|g" "app/src/main/java/com/teamz/lab/debugger/utils/rewarded_ad_manager.kt"
        echo "  ✅ Restored Rewarded Ad Unit ID"
    fi
fi

echo ""
echo "✅ All IDs restored! Your local files now have your actual IDs."
echo ""
echo "⚠️  Remember:"
echo "  - local_config.properties is in .gitignore and won't be committed"
echo "  - Your actual IDs are only in your local files"
echo "  - The repository still has placeholders for open source"

