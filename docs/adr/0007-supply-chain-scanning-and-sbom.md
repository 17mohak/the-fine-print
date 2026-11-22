# ADR-0007: Supply-chain controls — locking, checksum verification, SBOM, dependency and secret scanning

- **Status:** Accepted (engineering) — owner review at G0
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21: "CI (build, tests, ArchUnit, dependency/secret scan, SBOM)")
- **Decision owner:** Engineering
- **Related:** SECURITY_AND_PRIVACY T6, T8, T16; ADR-0002

## Decision
1. **Dependency locking** in STRICT mode for every configuration (`gradle.lockfile` per project,
   `settings-gradle.lockfile`). Nothing resolves unpinned. Regenerate with
   `./gradlew resolveAndLockAll --write-locks`.
2. **Dependency verification**: `gradle/verification-metadata.xml` holds a SHA-256 for every resolved artefact
   and POM, so a tampered or changed artefact fails the build. Regenerate with
   `./gradlew --write-verification-metadata sha256 build cyclonedxBom` and review the diff.
3. **Gradle wrapper**: the distribution SHA-256 is pinned in `gradle-wrapper.properties`. CI validates the
   wrapper JAR (`gradle/actions/setup-gradle`, `validate-wrappers: true`).
4. **SBOM**: CycloneDX Gradle plugin 3.6.1. `./gradlew cyclonedxBom` writes an aggregate
   `build/reports/cyclonedx/bom.{json,xml}` (CycloneDX 1.6) of the **runtime** classpaths, meaning what ships;
   test tooling is excluded. CI uploads it as an artefact.
5. **Dependency vulnerability scan**: OSV-Scanner (`google/osv-scanner-action` v2.7.1, SHA-pinned) over the
   committed lockfiles. It needs no API key, unlike OWASP Dependency-Check, which needs an NVD key.
6. **Secret scan**: gitleaks v8.30.1 (digest-pinned image) over the full git history, using `.gitleaks.toml`
   (default rules plus a path allow-list for the labelled development-only example files).
7. **CI hygiene**: `permissions: contents: read`; actions pinned by commit SHA; no secrets used by any job.

## Options considered
| Option | Pros | Cons |
|---|---|---|
| OSV-Scanner (chosen) | No key; reads Gradle lockfiles | Online database lookup |
| OWASP Dependency-Check | Widely known | NVD API key and slow updates |
| Trivy | Broad (images too) | Larger tool; image scanning not needed in P0 |
| gitleaks via the official action | Convenient | Organisation licence requirement for the action; the Docker image avoids it |

## Consequences
- Adding or upgrading a dependency is a three-step change: catalogue, then locks, then verification metadata.
  The resulting diff makes it reviewable.
- **Local limitation (P0 session):** OSV-Scanner and gitleaks were not run locally. Both are distributed here as
  container images or binaries, and the Docker engine was unavailable. They run only in CI, and CI has not run
  yet because there is no remote.

## Reversibility and revisit triggers
High. Revisit when container images are built for the app (add image scanning) and before P7 (signed builds).

## Deviation from the approved architecture?
No.

## Addendum 2026-11-20 — first scan results and resulting changes
Both scanners ran locally (pinned images) and in CI. gitleaks: no leaks, both with and without the allow-list.
OSV-Scanner found two advisories, recorded in `docs/evidence/p0/security-scans.md`:
1. Guava 33.7.1 on the build's own plugin classpath, via CycloneDX. Fixed with a buildscript constraint to
   33.7.2-jre.
2. commons-lang3 3.16.0, present only as a conflict-resolution candidate in the verification metadata. Treated
   as a false positive through an exact-version override with an expiry date in `osv-scanner.toml`, which CI passes
   with `--config`.

Two further decisions:
- The build's plugin classpath is now locked as well (`buildscript-gradle.lockfile`).
- Verification metadata must be regenerated from an empty cache (runbook), because a warm cache omits parent
  POMs.

Residual: the OSV action pulls its scanner image by tag, not digest.
