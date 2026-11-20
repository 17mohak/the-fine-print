# P0 security scans — results and findings

- **Date:** 2026-11-20
- **Scanners:** pinned exactly as in CI ([ADR-0007](../../adr/0007-supply-chain-scanning-and-sbom.md))
  - gitleaks `zricethezav/gitleaks:v8.30.1@sha256:c00b6bd0…bbb7f`
  - OSV-Scanner `ghcr.io/google/osv-scanner-action:v2.7.1@sha256:3fa25659…d08f243` (the image the CI action runs)
- **Raw outputs:** [scan-gitleaks.txt](scan-gitleaks.txt), [scan-osv.txt](scan-osv.txt) (commit `a401007`)

> Scope note: these scans check committed secrets and *known* vulnerabilities in declared dependencies. They are
> not a security review, and they make no claim that the system is secure (G0-SEC is still required before P1).

## Secret scan (gitleaks, `git` mode, full history)

| Run | Config | Commits | Result |
|---|---|---|---|
| Local, commit `93939ae` | `.gitleaks.toml` (default rules + dev-file path allow-list) | 8 | no leaks found |
| Local, commit `93939ae` | default rules only, **no allow-list** | 8 | no leaks found. The allow-list currently suppresses nothing |
| Local, commit `a401007` | `.gitleaks.toml` | 10 | no leaks found |
| CI run [44663133885](https://github.com/17mohak/the-fine-print/actions/runs/44663133885) | `.gitleaks.toml` | 8 | no leaks found |

The git-ignored local `ops/compose/.env` is never committed. It is excluded by `.gitignore`, which was verified with
`git check-ignore`.

## Dependency vulnerability scan (OSV-Scanner)

**Initial result** (local at `93939ae`, and CI run 44663133885, which failed this job): **2 findings**.

| Advisory | Package | Severity | Where it came from | Disposition |
|---|---|---|---|---|
| [GHSA-xxph-c9ww-hj94](https://osv.dev/GHSA-xxph-c9ww-hj94) / CVE-2026-102554: Guava CompactHashMap/CompactHashSet/MapMakerInternalMap deserialisation can trigger uncontrolled memory allocation | `com.google.guava:guava:33.7.1-jre` | High (CVSS 8.2) | The build's own plugin classpath, via the CycloneDX Gradle plugin 3.6.1 (`./gradlew buildEnvironment`). **Build-time only**: not on any application classpath and not in the SBOM | **Fixed** in `a401007`: a buildscript constraint to `33.7.2-jre` (the fixed version) |
| [GHSA-j288-q9x7-2f5v](https://osv.dev/GHSA-j288-q9x7-2f5v) / CVE-2025-48924: Commons Lang uncontrolled recursion on long inputs | `org.apache.commons:commons-lang3:3.16.0` | Medium (CVSS 6.5) | Only in `gradle/verification-metadata.xml`. Gradle downloads the metadata of versions it *considers* during conflict resolution. Every lockfile resolves **3.20.0** (patched), test-only, in `:api` | **False positive.** Narrow override in [`osv-scanner.toml`](../../../osv-scanner.toml): exact package and version, a written reason, and an expiry of 2027-05-20 |

**Changes made to resolve the findings (P0 build-tooling scope only):**
1. A constraint in the root `buildscript` (Guava 33.7.2-jre).
2. Dependency locking extended to the build's own plugin classpath (`buildscript-gradle.lockfile`), so that
   classpath is pinned and scanned. Previously it was neither.
3. `gradle/verification-metadata.xml` regenerated from an empty Gradle cache in a clean clone. The only removals
   were the two Guava 33.7.1 entries.
4. The CI OSV step now passes `--config=./osv-scanner.toml`.

**Result after the fix** (local at `a401007`): 26 lockfiles plus the verification metadata scanned,
**"No issues found"**, 1 package filtered by the documented override.

## Residual notes

- The OSV action's `action.yml` refers to its scanner image by **tag** (`v2.7.1`), not by digest. The action
  itself is SHA-pinned, but the tag it pulls could in principle be re-pointed. The image digest observed locally
  is recorded above.
- OSV coverage depends on the public advisory database at scan time. New advisories will appear on later runs;
  that is the intended behaviour.
- The override must be revisited when the build changes, and no later than its expiry date.
