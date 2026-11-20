# ADR-0002: P0 toolchain and exact versions

- **Status:** Proposed — **owner confirmation required at G0** (TECHNOLOGY_DECISIONS #2; D-21 clause 5)
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21)
- **Decision owner:** Engineering proposes; OWNER confirms
- **Related:** TECHNOLOGY_DECISIONS #1, #2, #7, #13, #20, #22; ADR-0005; ADR-0007

## Context
TECHNOLOGY_DECISIONS #2 recommends "Spring Boot (pinned current GA) + Gradle Kotlin DSL; Java 21 LTS", with
exact versions set by ADR and confirmed by the owner at G0. The versions were looked up from the publishers'
registries on **2026-11-20** (Maven Central metadata, Gradle Plugin Portal, services.gradle.org,
Docker Hub / Quay registry APIs, GitHub releases).

## Decision (proposed pins)
**Build and language**
| Item | Version | Notes |
|---|---|---|
| Java | 21 (LTS), `--release 21`, Gradle toolchain 21 | Local machine: Oracle JDK 21.0.4. CI: Temurin 21 (latest 21.x patch from `actions/setup-java`) |
| Gradle (wrapper) | 9.9.2 | Wrapper pins the distribution SHA-256 `971fcd71…914c76b` |
| Spring Boot | 4.2.0 | Current GA, released 2026-11-19 (the 4.1.x line is at 4.1.3). Brings Spring Framework 7.1.0 |
| JUnit | 6.1.3 | Same as Spring Boot 4.2.0's managed version |
| AssertJ | 3.27.7 | Same as Spring Boot 4.2.0's managed version |
| ArchUnit | 1.5.1 | Core library used from plain JUnit tests (no ArchUnit JUnit engine) |
| swagger-parser-v3 | 2.1.49 | Test-only; validates the OpenAPI skeleton |
| CycloneDX Gradle plugin | 3.6.1 | SBOM (CycloneDX 1.6) |

**Development containers (pinned by tag and digest)**
| Service | Image |
|---|---|
| PostgreSQL | `postgres:18.7-alpine3.23@sha256:f3ee138d…92fc41a0` (PostgreSQL 19 is only RC2, so not GA) |
| Object store | `chrislusf/seaweedfs:4.54@sha256:4b42c212…71222d15` (proposed MinIO substitute, ADR-0005) |
| Dev IdP | `quay.io/keycloak/keycloak:26.8.2@sha256:7e66a96e…eaba38b3` |
| Worker stub | `busybox:1.37.0-musl@sha256:f6a95d61…42454478` |
| gitleaks (CI) | `zricethezav/gitleaks:v8.30.1@sha256:c00b6bd0…bbb7f` |

**CI actions (pinned by commit SHA):** `actions/checkout` v7.0.1, `actions/setup-java` v6.3.0,
`gradle/actions` v6.4.1, `actions/upload-artifact` v7.0.2, `google/osv-scanner-action` v2.7.1.

Full values live only in `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`,
`ops/compose/compose.yaml` and `.github/workflows/ci.yml`. Exact resolved transitive versions are in the
committed `gradle.lockfile`s, and their SHA-256s in `gradle/verification-metadata.xml`.

## Options considered
| Option | Pros | Cons |
|---|---|---|
| Spring Boot 4.2.0 (chosen) | "Current GA" as the document specifies; longest support window | Only one day old: early patch releases are likely |
| Spring Boot 4.1.3 | More mature | Not "current GA"; an earlier upgrade is needed |
| PostgreSQL 18.7 (chosen) | Current stable major; meets "16+" | — |
| PostgreSQL 19 | Newest | Release candidate only |

## Consequences
- The owner may swap Spring Boot 4.2.0 for 4.1.3 at G0 with one line in the catalogue. Lockfiles and
  verification metadata then need regenerating.
- The local JDK (21.0.4, July 2024) is out of date for security patches. Updating it is recommended but not
  required to build.

## Reversibility and revisit triggers
High. Revisit at G0, on any CVE affecting a pinned component (OSV-Scanner in CI), and before P1.

## Deviation from the approved architecture?
No, apart from the object-store product (ADR-0005).
