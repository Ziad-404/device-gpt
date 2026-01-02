#!/bin/bash
# Comprehensive script to remove ALL secrets from git history
# WARNING: This rewrites git history - make sure you have a backup!

set -e

echo "⚠️  WARNING: This script will rewrite git history!"
echo "   This is a DESTRUCTIVE operation."
echo ""
echo "📋 What this script will do:"
echo "   1. Remove sensitive files from all commits"
echo "   2. Replace secrets with placeholders in all commits"
echo "   3. Clean up git history"
echo ""
read -p "Continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Aborted."
    exit 1
fi

echo ""
echo "🔍 Step 1: Creating backup branch..."
CURRENT_BRANCH=$(git branch --show-current)
BACKUP_BRANCH="backup-before-history-cleanup-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP_BRANCH"
echo "✅ Backup created: $BACKUP_BRANCH"
echo ""

echo "🔍 Step 2: Removing sensitive files from all commits..."
# Remove key.properties, google-services.json, local_config.properties from all commits
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch key.properties app/google-services.json local_config.properties' \
  --prune-empty --tag-name-filter cat -- --all

echo ""
echo "🔍 Step 3: Replacing secrets with placeholders in all commits..."

# List of secrets to replace
declare -A SECRETS=(
    ["ca-app-pub-7088022825081956~7696842403"]="YOUR_ADMOB_APP_ID"
    ["ca-app-pub-7088022825081956/3200748520"]="YOUR_APP_OPEN_AD_UNIT_ID"
    ["ca-app-pub-7088022825081956/5139119290"]="YOUR_INTERSTITIAL_AD_UNIT_ID"
    ["ca-app-pub-7088022825081956/2139601263"]="YOUR_NATIVE_AD_UNIT_ID"
    ["ca-app-pub-7088022825081956/1234567890"]="YOUR_REWARDED_AD_UNIT_ID"
    ["953087721962-4hd530vha827iq87ds66l11abibtla1g"]="YOUR_OAUTH_CLIENT_ID"
    ["1311bca6-33a9-48fc-bdfa-66ca5650806b"]="YOUR_ONESIGNAL_APP_ID"
)

# Replace each secret in all commits
for secret in "${!SECRETS[@]}"; do
    placeholder="${SECRETS[$secret]}"
    echo "   Replacing: $secret → $placeholder"
    
    # Escape special characters for sed
    escaped_secret=$(echo "$secret" | sed 's/[[\.*^$()+?{|]/\\&/g')
    
    git filter-branch --force --tree-filter \
      "find . -type f -not -path './.git/*' -exec sed -i '' 's|$escaped_secret|$placeholder|g' {} + 2>/dev/null || true" \
      --prune-empty --tag-name-filter cat -- --all
done

echo ""
echo "🔍 Step 4: Cleaning up git references..."
# Expire all reflog entries
git reflog expire --expire=now --all

# Garbage collect
git gc --prune=now --aggressive

echo ""
echo "🔍 Step 5: Verifying cleanup..."
echo ""

# Check if secrets still exist
SECRETS_FOUND=0
for secret in "${!SECRETS[@]}"; do
    if git log --all --source --full-history -p | grep -q "$secret"; then
        echo "   ⚠️  Still found: $secret"
        SECRETS_FOUND=$((SECRETS_FOUND + 1))
    fi
done

if [ $SECRETS_FOUND -eq 0 ]; then
    echo "✅ No secrets found in git history!"
else
    echo "⚠️  Warning: $SECRETS_FOUND secrets still found in history"
    echo "   You may need to run this script again or use BFG Repo-Cleaner"
fi

echo ""
echo "🔍 Step 6: Checking sensitive files..."
if git log --all --name-only | grep -qE "(key\.properties|google-services\.json|local_config\.properties)$"; then
    echo "   ⚠️  Sensitive files still in history"
else
    echo "   ✅ No sensitive files in history"
fi

echo ""
echo "✅ Git history cleanup complete!"
echo ""
echo "📋 Next steps:"
echo "   1. Review the changes: git log --all"
echo "   2. If everything looks good, force push to remote:"
echo "      git push --force --all"
echo "      git push --force --tags"
echo ""
echo "   ⚠️  WARNING: Force pushing will rewrite remote history!"
echo "   Make sure all team members are aware and have pulled the cleaned history."
echo ""
echo "   💾 Backup branch saved: $BACKUP_BRANCH"
echo "   To restore: git reset --hard $BACKUP_BRANCH"

