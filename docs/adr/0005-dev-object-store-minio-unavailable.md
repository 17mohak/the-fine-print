# ADR-0005: Development object store — MinIO is no longer distributed; propose SeaweedFS for dev only

- **Status:** **Accepted for development only** — owner decision D-32 (2026-11-23). Not a production storage decision. Originally Proposed for G0.
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21: "Docker Compose dev stack with stubs … MinIO")
- **Decision owner:** OWNER (the product name appears in the approved roadmap). The production object store stays OWNER + LEGAL (TECHNOLOGY_DECISIONS #9)
- **Related:** IMPLEMENTATION_ROADMAP P0; TECHNOLOGY_DECISIONS #9; SECURITY_AND_PRIVACY section 1

## Context
The roadmap names MinIO for the P0 dev stack. Checked on 2026-11-20 **[VERIFIED FACT]**:
- `hub.docker.com/v2/repositories/minio/minio/` returns HTTP 404 (no public image repository).
- GitHub `minio/minio` is **archived**. Its last release is `RELEASE.2025-10-15T17-29-55Z` (2025-10-16).

There is therefore no maintained, officially distributed MinIO image to pin. The architecture actually requires
an "S3-compatible" interface (TECHNOLOGY_DECISIONS #9); the product choice is not architectural for development.

## Decision (proposed)
Use **SeaweedFS 4.54** (Apache-2.0; GitHub release 2026-11-16; not archived) with its S3 gateway as the
**development-only** S3-compatible stub, pinned by digest. Credentials are clearly fake, development-only
values. No buckets, lifecycle rules or encryption are configured: storage encryption is P1 after G0-SEC.
Nothing in P0 code talks to it.

This does **not** choose a production object store.

## Options considered
| Option | Pros | Cons |
|---|---|---|
| SeaweedFS (proposed) | Maintained, Apache-2.0, single container, S3 API | Different S3 edge-case behaviour from AWS/MinIO; untested when first written (Docker unavailable); since verified, see the 2026-11-20 addendum |
| Garage | Maintained, light | AGPL; needs a post-start layout/key bootstrap step |
| Pin an old third-party MinIO image or a community fork | Matches the roadmap name | Unmaintained or unofficial supply chain; unpatched |
| Build MinIO from archived source | Official code | No security fixes; build burden |
| LocalStack S3 | Popular for tests | Emulator; licensing and feature changes |

## Consequences
- Until the owner confirms, the substitute is provisional. If rejected, only `ops/compose/compose.yaml`
  (one service) and `ops/scripts/verify-dev-stack.sh` change.
- S3-compatibility gaps may surface in P1. P1 integration tests should run against the chosen store.

## Reversibility and revisit triggers
High. Revisit at G0, at the production hosting decision (TECHNOLOGY_DECISIONS #9, #12), or if SeaweedFS
maintenance changes.

## Deviation from the approved architecture?
**Yes (product named in the roadmap).** Approved by the owner for development only (DECISION_LOG D-32).

## Addendum 2026-11-20 — development behaviour verified (status unchanged: still Proposed)
SeaweedFS 4.54 started healthy and passed the stack checks. S3 requests are refused without credentials (403) or
with a wrong secret (403). The dev identity's ListBuckets returns 200 with zero buckets. Only the S3 port
(127.0.0.1:8333) is published. **This verifies the development stub only. The owner decision on the substitution
is still required at G0.**

## Addendum 2026-11-23 — owner decision D-32 (status: Accepted for development only)
The owner keeps SeaweedFS 4.54 as the development-only S3-compatible stand-in (DECISION_LOG D-32). **This is not a
choice of production object storage**, which stays OWNER + LEGAL (TECHNOLOGY_DECISIONS #9, #12). The P1 conditions in
`docs/g0/G0-3_OBJECT_STORE_ASSESSMENT.md` section 3 still apply: standard S3 API only, and no reliance on SeaweedFS-specific behaviour.
