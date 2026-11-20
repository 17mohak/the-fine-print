# G0-1 — Exact toolchain inventory (2026-11-20)

- **Decision status:** Spring Boot **4.2.0 / 4.2.x line — DECIDED by the owner (DECISION_LOG D-30)**. **Every
  other row is PROPOSED** (ADR-0002) and awaits owner confirmation. No version was changed while preparing this
  inventory.
- **Legend:**
  - *Verified*: exercised by the P0 build, tests, CI or the stack checks.
  - *Inspected*: read or looked up, not exercised.
- **Sources:** the repository files named in the table; Maven Central, the Gradle Plugin Portal and
  services.gradle.org; GitHub releases; endoflife.date (API, 2026-11-20); spring.io generations API; OSV-Scanner
  and Trivy results in `docs/evidence/p0/`.

## 1. Language, build and framework

| Component | Pinned | Where pinned | Verified | Support / security status (2026-11-20) | Owner decision |
|---|---|---|---|---|---|
| Java (language level) | 21 (`--release 21`, toolchain 21) | `build-logic/.../JavaConventionsPlugin.java`, `build-logic/build.gradle.kts` | Verified | LTS. Temurin 21 supported to 2029-12-31; Oracle JDK 21 Premier to 2028-09-30 | Confirm (TECHNOLOGY_DECISIONS #1/#2) |
| JDK — CI | Temurin `21` (**latest 21.x at run time; patch level floats**) | `.github/workflows/ci.yml` (`actions/setup-java`) | Verified | Current patch is 21.0.13 | Confirm whether a floating patch is acceptable (recommended: yes, for security patches) |
| JDK — local machine | Oracle 21.0.4 (not pinned by the repository) | developer machine | Verified | **9 patches behind 21.0.13**; missing security fixes | Recommend updating; not blocking |
| Gradle | 9.9.2 | `gradle/wrapper/gradle-wrapper.properties` (with distribution SHA-256) | Verified | Current release | Confirm |
| Spring Boot (BOM + Gradle plugin) | **4.2.0** | `gradle/libs.versions.toml` (`spring-boot`) | Verified | 4.2.x OSS support to 2027-12-31 (spring.io). The **latest 4.2.x patch must be evaluated before P1 (D-30)** | **DECIDED (D-30)** |
| Spring Framework (managed) | 7.1.0 | via the Boot BOM; `platform/app/gradle.lockfile` | Verified | Follows the Boot line | Covered by D-30 |
| Embedded Tomcat (managed) | 11.0.27 | via the Boot BOM; lockfile | Verified | 11.0.29 exists upstream. **No OSV advisory against 11.0.27** at scan time. Will move with the next Boot patch | Covered by D-30 (no separate pin) |
| Other Boot-managed runtime libraries | e.g. Logback 1.6.6, Micrometer 1.18.0, Jackson annotations 2.21 | via the Boot BOM; lockfile | Verified (OSV clean) | Managed by Boot | Covered by D-30 |

## 2. Test and build tooling

| Component | Pinned | Where pinned | Verified | Status | Owner decision |
|---|---|---|---|---|---|
| JUnit (BOM) | 6.1.3 | `gradle/libs.versions.toml` | Verified | Latest stable; matches the Boot 4.2.0 managed version | Confirm |
| AssertJ | 3.27.7 | `gradle/libs.versions.toml` | Verified | Latest stable | Confirm |
| ArchUnit | 1.5.1 | `gradle/libs.versions.toml` | Verified | Latest stable | Confirm |
| swagger-parser-v3 (test-only) | 2.1.49 | `gradle/libs.versions.toml` | Verified | Latest stable | Confirm |
| CycloneDX Gradle plugin | 3.6.1 | `gradle/libs.versions.toml` | Verified | Latest | Confirm |
| Guava (build-time, transitive via CycloneDX) | **33.7.2-jre** (security constraint) | root `build.gradle.kts` buildscript constraint; `buildscript-gradle.lockfile` | Verified | Fixes GHSA-xxph-c9ww-hj94 | Confirm (security fix) |
| Other plugin-classpath dependencies | e.g. cyclonedx-core-java 13.4.0, Jackson 2.22.3 | `buildscript-gradle.lockfile` | Verified (OSV clean) | — | Covered by the CycloneDX pin |
| Spring Boot Gradle plugin transitive classpath | resolved from the plugin's fixed POM | not separately locked (see finding F-3) | Verified via checksums in `gradle/verification-metadata.xml` | OSV clean (metadata scanned) | — |

## 3. Development containers (digest-pinned in `ops/compose/compose.yaml`)

| Component | Pinned | Verified | Status (2026-11-20) | Known HIGH/CRITICAL (Trivy) | Owner decision |
|---|---|---|---|---|---|
| PostgreSQL | `postgres:18.7-alpine3.23@sha256:f3ee138d…` | Verified | 18.7 is the latest 18.x; PostgreSQL 18 supported to 2030-11-14; Alpine 3.23 to 2027-11-01 | 27, all in the bundled **gosu** helper (Go stdlib), not PostgreSQL | Confirm |
| Keycloak (dev IdP) | `quay.io/keycloak/keycloak:26.8.2@sha256:7e66a96e…` | Verified | Latest release. Community Keycloak supports the latest release only (vendor practice; inspected) | 6: base-OS sqlite/OpenJDK (no fix yet) and bundled Guava 33.6.0 | Confirm |
| SeaweedFS (proposed MinIO substitute) | `chrislusf/seaweedfs:4.54@sha256:4b42c212…` | Verified | Latest release; active project | 0 | **G0-3 open** (ADR-0005) |
| busybox (worker stub) | `busybox:1.37.0-musl@sha256:f6a95d61…` | Verified | **1.38.0 now available** | 0 | Confirm 1.37.0, or approve 1.38.0 |

## 4. Security tooling and CI actions

| Component | Pinned | Where | Verified | Status | Owner decision |
|---|---|---|---|---|---|
| gitleaks | v8.30.1 `@sha256:c00b6bd0…` | `ci.yml`; runbook | Verified (local + CI) | Latest release (2026-03-21) | Confirm |
| OSV-Scanner (action) | v2.7.1, action SHA `32d3cd48…`; image observed as `@sha256:3fa25659…` | `ci.yml` | Verified (local + CI) | Latest. **The action pulls its image by tag, not digest** | Confirm |
| actions/checkout | v7.0.1 (`3d3c42e5…`) | `ci.yml` | Verified | — | Confirm |
| actions/setup-java | v6.3.0 (`d1374f8a…`) | `ci.yml` | Verified | — | Confirm |
| gradle/actions (setup-gradle) | v6.4.1 (`8da46b53…`) | `ci.yml` | Verified | — | Confirm |
| actions/upload-artifact | v7.0.2 (`cf430e03…`) | `ci.yml` | Verified | — | Confirm |
| CI runner | `ubuntu-24.04` | `ci.yml` | Verified | GitHub-hosted | Confirm |
| Trivy (informational, not in CI) | 0.76.2 `@sha256:8f6bb947…` | used ad hoc only | Inspected | — | Optional: add image scanning to CI (pipeline change) |

## 5. Findings and recommendations (nothing changed)

| # | Finding | Evidence | Recommendation | Needs |
|---|---|---|---|---|
| F-1 | Local JDK 21.0.4 lacks 9 patch releases of fixes | endoflife.date: latest 21.0.13 | Update the local JDK to the current 21.0.x | Owner action (machine) |
| F-2 | CI Java patch level floats (`java-version: "21"`) | `ci.yml` | Keep floating to pick up security patches, and record the decision; or pin `21.0.13` for strict reproducibility | Owner choice |
| F-3 | The Spring Boot Gradle plugin's transitive classpath (`:platform:app` buildscript) is not lockfile-locked; it is checksum-verified | `buildscript-gradle.lockfile` covers the root only | Activate locking on that buildscript classpath too (build hygiene; no version change) | Owner OK to change the build config on the P0 branch |
| F-4 | Postgres image: 27 Go-stdlib CVEs in `gosu` | `image-scan-trivy.md` | Accept for the dev stub; re-pin when upstream rebuilds | Owner acknowledgement |
| F-5 | Keycloak image: 6 HIGH (no OS fix yet; bundled Guava) | `image-scan-trivy.md` | Accept for the dev stub; re-pin at the next Keycloak release | Owner acknowledgement |
| F-6 | busybox 1.38.0 available | Docker Hub | Optional bump (the worker stub is inert and network-less) | Owner choice |
| F-7 | No container-image scanning in CI | ADR-0007 | Add a pinned Trivy step (informational, then gating) before P1 builds application images | Owner approval (pipeline change) |
| F-8 | Spring Boot 4.2.0 is a `.0` release; Tomcat 11.0.29 exists | spring.io; endoflife.date | Already covered: evaluate the latest 4.2.x patch before P1 (D-30) | — |

**Unsupported components:** none found. **Pinned Maven dependency with a known advisory:** none after the Guava
fix (OSV clean; one documented false-positive override).
