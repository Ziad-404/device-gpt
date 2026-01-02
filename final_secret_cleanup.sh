#!/bin/bash
# Final comprehensive secret replacement - does all at once

set -e
export FILTER_BRANCH_SQUELCH_WARNING=1

echo "🔍 Replacing ALL secrets with placeholders in git history..."
echo "⏱️  This will process all commits - may take a few minutes..."
echo ""

# Single filter-branch command that replaces all secrets at once
git filter-branch --force --tree-filter '
  find . -type f -not -path "./.git/*" -print0 | while IFS= read -r -d "" file; do
    # Skip binary files and git files
    if file "$file" | grep -q "text"; then
      sed -i "" \
        -e "s|ca-app-pub-7088022825081956~7696842403|YOUR_ADMOB_APP_ID|g" \
        -e "s|ca-app-pub-7088022825081956/3200748520|YOUR_APP_OPEN_AD_UNIT_ID|g" \
        -e "s|ca-app-pub-7088022825081956/5139119290|YOUR_INTERSTITIAL_AD_UNIT_ID|g" \
        -e "s|ca-app-pub-7088022825081956/2139601263|YOUR_NATIVE_AD_UNIT_ID|g" \
        -e "s|ca-app-pub-7088022825081956/1234567890|YOUR_REWARDED_AD_UNIT_ID|g" \
        -e "s|953087721962-4hd530vha827iq87ds66l11abibtla1g|YOUR_OAUTH_CLIENT_ID|g" \
        -e "s|1311bca6-33a9-48fc-bdfa-66ca5650806b|YOUR_ONESIGNAL_APP_ID|g" \
        "$file" 2>/dev/null || true
    fi
  done
' --prune-empty --tag-name-filter cat -- --all

echo ""
echo "✅ All secrets replaced!"
echo ""
echo "🔍 Cleaning up..."
rm -rf .git/refs/original/
git reflog expire --expire=now --all
git gc --prune=now --aggressive

echo ""
echo "✅ Git history cleanup complete!"

