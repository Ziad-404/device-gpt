#!/bin/bash
# COMPREHENSIVE secret removal from ALL git history
# This removes ALL instances of secrets, including in diffs

set -e
export FILTER_BRANCH_SQUELCH_WARNING=1

echo "🔒 COMPREHENSIVE SECRET REMOVAL"
echo "================================"
echo ""
echo "This will remove ALL secrets from git history including:"
echo "  - All AdMob IDs"
echo "  - OAuth Client ID"
echo "  - OneSignal App IDs"
echo "  - Keystore files"
echo ""

# Remove keystore files first
echo "Step 1: Removing keystore files from history..."
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch app/release-key.jks app/*.jks "app/.!*" 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

# Remove all sensitive files
echo "Step 2: Removing sensitive files from history..."
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch key.properties app/google-services.json local_config.properties 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

# Replace ALL secrets in one comprehensive pass
echo "Step 3: Replacing ALL secrets with placeholders..."
git filter-branch --force --tree-filter '
  find . -type f -not -path "./.git/*" -print0 | while IFS= read -r -d "" file; do
    if [ -f "$file" ] && file "$file" | grep -q "text"; then
      # Replace all AdMob IDs
      sed -i "" \
        -e "s|ca-app-pub-7088022825081956~7696842403|YOUR_ADMOB_APP_ID|g" \
        -e "s|ca-app-pub-7088022825081956/3200748520|YOUR_APP_OPEN_AD_UNIT_ID|g" \
        -e "s|ca-app-pub-7088022825081956/5139119290|YOUR_INTERSTITIAL_AD_UNIT_ID|g" \
        -e "s|ca-app-pub-7088022825081956/2139601263|YOUR_NATIVE_AD_UNIT_ID|g" \
        -e "s|ca-app-pub-7088022825081956/1234567890|YOUR_REWARDED_AD_UNIT_ID|g" \
        -e "s|ca-app-pub-7088022825081956/7746279029|YOUR_AD_UNIT_ID|g" \
        -e "s|953087721962-4hd530vha827iq87ds66l11abibtla1g|YOUR_OAUTH_CLIENT_ID|g" \
        -e "s|1311bca6-33a9-48fc-bdfa-66ca5650806b|YOUR_ONESIGNAL_APP_ID|g" \
        -e "s|6827d1dd-3f10-800f-b99e-85f2e7deeda4|YOUR_ONESIGNAL_APP_ID|g" \
        "$file" 2>/dev/null || true
    fi
  done
' --prune-empty --tag-name-filter cat -- --all

echo ""
echo "Step 4: Final cleanup..."
rm -rf .git/refs/original/
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo ""
echo "✅ Comprehensive cleanup complete!"

