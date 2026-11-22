#!/usr/bin/env bash
# Enforces the commit-attribution rules in CONTRIBUTING.md (ADR-0008):
#   - no AI co-author trailers (Claude, Anthropic, or any noreply@anthropic.com address);
#   - no commit whose author or committer identity is an AI identity.
# Usage: check-commit-attribution.sh [<revision-range>]   (default: all commits reachable from HEAD)
set -euo pipefail

range="${1:-HEAD}"
# Matches AI product names and Anthropic addresses, not human names such as "Claude Martin".
pattern='(anthropic|claude[ -]?(code|opus|sonnet|haiku|fable|ai)([^a-z]|$))'
bad=0

while IFS= read -r sha; do
  identities="$(git log -1 --format='%an <%ae>%n%cn <%ce>' "$sha")"
  trailers="$(git log -1 --format='%B' "$sha" | grep -iE '^[[:space:]]*co-authored-by:' || true)"
  if printf '%s\n' "$identities" | grep -qiE "$pattern"; then
    echo "FAIL $sha: AI identity used as author/committer"
    bad=1
  fi
  if printf '%s\n' "$trailers" | grep -qiE "$pattern"; then
    echo "FAIL $sha: AI co-author trailer present"
    bad=1
  fi
done < <(git rev-list "$range")

if [ "$bad" -ne 0 ]; then
  echo "Commit-attribution check failed. See CONTRIBUTING.md#commit-attribution."
  exit 1
fi
echo "Commit-attribution check passed for $(git rev-list --count "$range") commit(s)"
