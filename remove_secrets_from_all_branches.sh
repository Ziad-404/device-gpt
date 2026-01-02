#!/bin/bash
# Comprehensive script to remove secrets and replace IDs with placeholders from all branches

set -e

CURRENT_BRANCH=$(git branch --show-current)
echo "Current branch: $CURRENT_BRANCH"
echo ""

# Function to replace secrets in files
replace_secrets_in_branch() {
    local branch=$1
    echo "Processing $branch..."
    git checkout "$branch" 2>/dev/null || {
        echo "⚠️  Could not checkout $branch, skipping..."
        return
    }
    
    local has_changes=false
    
    # Remove sensitive files from git tracking
    if git ls-files --error-unmatch key.properties app/google-services.json >/dev/null 2>&1; then
        git rm --cached key.properties app/google-services.json 2>/dev/null || true
        has_changes=true
    fi
    
    # Replace AdMob App ID in AndroidManifest.xml
    if [ -f "app/src/main/AndroidManifest.xml" ]; then
        if grep -q "YOUR_ADMOB_APP_ID" "app/src/main/AndroidManifest.xml"; then
            sed -i '' 's/YOUR_ADMOB_APP_ID/YOUR_ADMOB_APP_ID/g' "app/src/main/AndroidManifest.xml"
            git add "app/src/main/AndroidManifest.xml"
            has_changes=true
            echo "  ✅ Replaced AdMob App ID in AndroidManifest.xml"
        fi
    fi
    
    # Replace OAuth Client ID in strings.xml
    if [ -f "app/src/main/res/values/strings.xml" ]; then
        if grep -q "YOUR_OAUTH_CLIENT_ID" "app/src/main/res/values/strings.xml"; then
            sed -i '' 's/YOUR_OAUTH_CLIENT_ID\.apps\.googleusercontent\.com/YOUR_OAUTH_CLIENT_ID.apps.googleusercontent.com/g' "app/src/main/res/values/strings.xml"
            git add "app/src/main/res/values/strings.xml"
            has_changes=true
            echo "  ✅ Replaced OAuth Client ID in strings.xml"
        fi
    fi
    
    # Replace AdMob Ad Unit IDs in app_open_manager.kt
    if [ -f "app/src/main/java/com/teamz/lab/debugger/utils/app_open_manager.kt" ]; then
        if grep -q "YOUR_APP_OPEN_AD_UNIT_ID" "app/src/main/java/com/teamz/lab/debugger/utils/app_open_manager.kt"; then
            sed -i '' 's/ca-app-pub-7088022825081956\/3200748520/YOUR_APP_OPEN_AD_UNIT_ID/g' "app/src/main/java/com/teamz/lab/debugger/utils/app_open_manager.kt"
            git add "app/src/main/java/com/teamz/lab/debugger/utils/app_open_manager.kt"
            has_changes=true
            echo "  ✅ Replaced App Open Ad Unit ID"
        fi
    fi
    
    # Replace AdMob Ad Unit IDs in interstitial_ad_manager.kt
    if [ -f "app/src/main/java/com/teamz/lab/debugger/utils/interstitial_ad_manager.kt" ]; then
        if grep -q "YOUR_INTERSTITIAL_AD_UNIT_ID" "app/src/main/java/com/teamz/lab/debugger/utils/interstitial_ad_manager.kt"; then
            sed -i '' 's/ca-app-pub-7088022825081956\/5139119290/YOUR_INTERSTITIAL_AD_UNIT_ID/g' "app/src/main/java/com/teamz/lab/debugger/utils/interstitial_ad_manager.kt"
            git add "app/src/main/java/com/teamz/lab/debugger/utils/interstitial_ad_manager.kt"
            has_changes=true
            echo "  ✅ Replaced Interstitial Ad Unit ID"
        fi
    fi
    
    # Replace AdMob Ad Unit IDs in expandable_info_list.kt
    if [ -f "app/src/main/java/com/teamz/lab/debugger/ui/expandable_info_list.kt" ]; then
        if grep -q "YOUR_NATIVE_AD_UNIT_ID" "app/src/main/java/com/teamz/lab/debugger/ui/expandable_info_list.kt"; then
            sed -i '' 's/ca-app-pub-7088022825081956\/2139601263/YOUR_NATIVE_AD_UNIT_ID/g' "app/src/main/java/com/teamz/lab/debugger/ui/expandable_info_list.kt"
            git add "app/src/main/java/com/teamz/lab/debugger/ui/expandable_info_list.kt"
            has_changes=true
            echo "  ✅ Replaced Native Ad Unit ID"
        fi
    fi
    
    # Replace AdMob Ad Unit IDs in rewarded_ad_manager.kt (if it's not already a placeholder)
    if [ -f "app/src/main/java/com/teamz/lab/debugger/utils/rewarded_ad_manager.kt" ]; then
        if grep -q "YOUR_REWARDED_AD_UNIT_ID" "app/src/main/java/com/teamz/lab/debugger/utils/rewarded_ad_manager.kt"; then
            sed -i '' 's/ca-app-pub-7088022825081956\/1234567890/YOUR_REWARDED_AD_UNIT_ID/g' "app/src/main/java/com/teamz/lab/debugger/utils/rewarded_ad_manager.kt"
            git add "app/src/main/java/com/teamz/lab/debugger/utils/rewarded_ad_manager.kt"
            has_changes=true
            echo "  ✅ Replaced Rewarded Ad Unit ID"
        fi
    fi
    
    # Update .gitignore if needed
    if ! grep -q "key.properties" .gitignore 2>/dev/null; then
        echo "" >> .gitignore
        echo "# Sensitive files - DO NOT COMMIT" >> .gitignore
        echo "key.properties" >> .gitignore
        echo "app/google-services.json" >> .gitignore
        echo "release-key.jks" >> .gitignore
        echo "*.jks" >> .gitignore
        echo "*.keystore" >> .gitignore
        git add .gitignore
        has_changes=true
    fi
    
    # Commit if there are changes
    if [ "$has_changes" = true ]; then
        git commit -m "Remove secrets and replace IDs with placeholders for open source" || echo "  ℹ️  No changes to commit"
        echo "  ✅ Committed changes in $branch"
    else
        echo "  ℹ️  No changes needed in $branch"
    fi
    
    echo ""
}

echo "Step 1: Committing current changes in $CURRENT_BRANCH..."
git add .
git commit -m "Prepare for open source: remove secrets and replace IDs" || echo "Nothing to commit in current branch"
echo ""

echo "Step 2: Processing all branches..."
echo ""

# Process each branch
for branch in main release-2.0.1 release-3.0.1; do
    replace_secrets_in_branch "$branch"
done

# Return to original branch
echo "Returning to original branch: $CURRENT_BRANCH"
git checkout "$CURRENT_BRANCH" 2>/dev/null || echo "Could not return to $CURRENT_BRANCH"

echo ""
echo "✅ Done! Verifying all branches..."
echo ""
for branch in main release-2.0.1 release-3.0.1; do
    echo "=== $branch ==="
    # Check for sensitive files
    if git ls-tree -r --name-only "$branch" 2>/dev/null | grep -qE "^(key\.properties|app/google-services\.json)$"; then
        echo "  ❌ Still has sensitive files"
    else
        echo "  ✅ No sensitive files"
    fi
    # Check for production AdMob IDs
    if git grep -q "ca-app-pub-7088022825081956" "$branch" 2>/dev/null; then
        echo "  ⚠️  Still has production AdMob IDs"
    else
        echo "  ✅ No production AdMob IDs"
    fi
    # Check for OAuth Client ID
    if git grep -q "YOUR_OAUTH_CLIENT_ID" "$branch" 2>/dev/null; then
        echo "  ⚠️  Still has OAuth Client ID"
    else
        echo "  ✅ No OAuth Client ID"
    fi
    echo ""
done

echo "✅ All branches processed!"
