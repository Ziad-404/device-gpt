#!/bin/bash

# Safe script to remove sensitive files from all branches
# This uses git commands that work without checking out branches

set -e

echo "⚠️  Removing sensitive files from all branches"
echo ""

SENSITIVE_FILES=("key.properties" "app/google-services.json" "release-key.jks")
CURRENT_BRANCH=$(git branch --show-current)

echo "Current branch: $CURRENT_BRANCH"
echo "Files to remove: ${SENSITIVE_FILES[*]}"
echo ""

# Get all local branches
BRANCHES=$(git branch | sed 's/^[* ] //' | grep -v '^HEAD')

for branch in $BRANCHES; do
    echo "=========================================="
    echo "Processing branch: $branch"
    echo "=========================================="
    
    # Check which files exist in this branch
    FILES_TO_REMOVE=()
    for file in "${SENSITIVE_FILES[@]}"; do
        if git ls-tree -r --name-only "$branch" | grep -q "^${file}$"; then
            FILES_TO_REMOVE+=("$file")
            echo "  Found: $file"
        fi
    done
    
    if [ ${#FILES_TO_REMOVE[@]} -eq 0 ]; then
        echo "✅ No sensitive files in $branch"
    else
        echo "Removing files from $branch..."
        
        # Use git update-index to remove from the branch's index
        # We'll need to checkout, remove, and commit
        # But first, let's create a script that the user can run
        
        echo "  To remove from $branch, run:"
        echo "    git checkout $branch"
        echo "    git rm --cached ${FILES_TO_REMOVE[*]}"
        echo "    git commit -m 'Remove sensitive files'"
        echo ""
    fi
done

echo ""
echo "=========================================="
echo "Manual steps needed:"
echo "=========================================="
echo ""
echo "Since you have uncommitted changes, please:"
echo ""
echo "1. Commit or stash your current changes:"
echo "   git add ."
echo "   git commit -m 'Prepare for open source: remove secrets'"
echo "   OR"
echo "   git stash"
echo ""
echo "2. Then run this for each branch:"
echo ""
for branch in $BRANCHES; do
    if [ "$branch" != "$CURRENT_BRANCH" ]; then
        echo "   git checkout $branch"
        echo "   git rm --cached key.properties app/google-services.json 2>/dev/null || true"
        echo "   git commit -m 'Remove sensitive files' || echo 'Already removed'"
        echo ""
    fi
done
echo "3. Return to your branch:"
echo "   git checkout $CURRENT_BRANCH"
echo ""

