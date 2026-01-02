#!/bin/bash
# Script to create a fresh repository for open source
# This is the SAFEST option - no risk of exposed secrets

set -e

echo "🚀 Creating Fresh Repository for Open Source"
echo "=============================================="
echo ""
echo "This script will:"
echo "  1. Create a fresh git repository"
echo "  2. Add all current files (secrets already removed)"
echo "  3. Create initial commit"
echo "  4. Prepare for pushing to new GitHub repository"
echo ""
read -p "Continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Aborted."
    exit 1
fi

echo ""
echo "Step 1: Backing up current .git..."
if [ -d ".git" ]; then
    mv .git .git.backup.$(date +%Y%m%d-%H%M%S)
    echo "✅ Current .git backed up"
fi

echo ""
echo "Step 2: Initializing fresh repository..."
git init
git branch -M main

echo ""
echo "Step 3: Adding all files..."
git add .

echo ""
echo "Step 4: Creating initial commit..."
git commit -m "Initial commit - open source ready

- All secrets removed
- Configuration externalized to local_config.properties
- Template files provided
- Ready for open source contribution"

echo ""
echo "✅ Fresh repository created!"
echo ""
echo "📋 Next steps:"
echo "  1. Create a NEW repository on GitHub:"
echo "     https://github.com/new"
echo "     Name: debugger (or debugger-open-source)"
echo "     Description: [Your description]"
echo "     Visibility: Private (for now)"
echo ""
echo "  2. Add the new remote:"
echo "     git remote add origin https://github.com/Teamz-Lab-LTD/debugger.git"
echo ""
echo "  3. Push to new repository:"
echo "     git push -u origin main"
echo ""
echo "  4. Verify no secrets:"
echo "     git log -p | grep -E 'ca-app-pub-7088022825081956|953087721962|1311bca6'"
echo "     (Should return nothing)"
echo ""
echo "  5. Make repository public when ready"
echo ""
echo "💾 Backup saved: .git.backup.*"
echo "   You can restore with: mv .git.backup.* .git"

