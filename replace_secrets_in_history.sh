#!/bin/bash
# Replace secrets with placeholders in git history

set -e

echo "🔍 Replacing secrets with placeholders in all commits..."
echo ""

# Replace each secret individually to avoid shell parsing issues
echo "   Replacing AdMob App ID..."
git filter-branch --force --tree-filter \
  'find . -type f -not -path "./.git/*" -exec sed -i "" "s|ca-app-pub-7088022825081956~7696842403|YOUR_ADMOB_APP_ID|g" {} + 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

echo "   Replacing App Open Ad Unit ID..."
git filter-branch --force --tree-filter \
  'find . -type f -not -path "./.git/*" -exec sed -i "" "s|ca-app-pub-7088022825081956/3200748520|YOUR_APP_OPEN_AD_UNIT_ID|g" {} + 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

echo "   Replacing Interstitial Ad Unit ID..."
git filter-branch --force --tree-filter \
  'find . -type f -not -path "./.git/*" -exec sed -i "" "s|ca-app-pub-7088022825081956/5139119290|YOUR_INTERSTITIAL_AD_UNIT_ID|g" {} + 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

echo "   Replacing Native Ad Unit ID..."
git filter-branch --force --tree-filter \
  'find . -type f -not -path "./.git/*" -exec sed -i "" "s|ca-app-pub-7088022825081956/2139601263|YOUR_NATIVE_AD_UNIT_ID|g" {} + 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

echo "   Replacing Rewarded Ad Unit ID..."
git filter-branch --force --tree-filter \
  'find . -type f -not -path "./.git/*" -exec sed -i "" "s|ca-app-pub-7088022825081956/1234567890|YOUR_REWARDED_AD_UNIT_ID|g" {} + 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

echo "   Replacing OAuth Client ID..."
git filter-branch --force --tree-filter \
  'find . -type f -not -path "./.git/*" -exec sed -i "" "s|953087721962-4hd530vha827iq87ds66l11abibtla1g|YOUR_OAUTH_CLIENT_ID|g" {} + 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

echo "   Replacing OneSignal App ID..."
git filter-branch --force --tree-filter \
  'find . -type f -not -path "./.git/*" -exec sed -i "" "s|1311bca6-33a9-48fc-bdfa-66ca5650806b|YOUR_ONESIGNAL_APP_ID|g" {} + 2>/dev/null || true' \
  --prune-empty --tag-name-filter cat -- --all

echo ""
echo "✅ Secret replacement complete!"

