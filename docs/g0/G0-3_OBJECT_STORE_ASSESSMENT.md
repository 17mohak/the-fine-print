# G0-3 — Development object store: SeaweedFS as a MinIO stand-in — assessment

- **Prepared:** 2026-11-20. **Status: PROPOSED — awaiting owner approval** (ADR-0005; a deviation from the
  roadmap's named product). **Not approved.**
- **Scope:** the *development* S3-compatible stub only. The production object store is a separate OWNER + LEGAL
  decision (TECHNOLOGY_DECISIONS #9, #12) and is not affected.

## 1. Why a substitute is needed (verified 2026-11-20)
- `hub.docker.com/v2/repositories/minio/minio/` returns HTTP 404.
- GitHub `minio/minio` is **archived**; its last release is `RELEASE.2025-10-15T17-29-55Z`.
- There is no maintained, officially distributed MinIO image to pin.

## 2. SeaweedFS facts

| Aspect | Finding | Basis |
|---|---|---|
| Maintenance | Active. 12 releases between 2026-08-21 and 2026-11-16 (4.43 → 4.54); last push 2026-11-20; about 500 contributors in GitHub stats | GitHub API, 2026-11-20 (verified) |
| Licence | Apache-2.0 | GitHub API (verified) |
| Image | `chrislusf/seaweedfs:4.54`, pinned by digest; latest release | Registry and GitHub (verified) |
| Known image CVEs | Trivy 0.76.2: **0 HIGH/CRITICAL** | `docs/evidence/p0/image-scan-trivy.md` (verified) |
| S3 API coverage | Broad: bucket/object CRUD, multipart upload, versioning, object lock, SSE-S3/SSE-KMS/SSE-C, bucket policies and ACLs, conditional headers, range reads, presigned/POST policy | SeaweedFS wiki "Amazon S3 API" (inspected, not tested) |
| Documented S3 differences | No multi-range reads; deleting an object can delete a "folder"; a path cannot be both file and folder; only `/` as delimiter; no MFA delete; lifecycle *transition* rules unsupported | Same wiki (inspected) |
| Authentication | SigV4. In the dev stack: anonymous → 403, wrong secret → 403, dev identity ListBuckets → 200 with 0 buckets | `dev-stack-inspection.txt` (verified) |
| Local suitability | Single container, starts in seconds, healthy in CI and locally; low resource use | Evidence revisions 2–3 (verified) |
| Operational complexity (dev) | Low: one `server -s3` process plus one JSON identity file. Internal ports (master 9333, volume 8080, filer 8888) are not published | `compose.yaml`; inspection (verified) |

## 3. Does the Compose setup rely on SeaweedFS-specific behaviour?
- **Application code:** no. Nothing in P0 talks to the object store.
- **Compose and verification (SeaweedFS-specific):** the command flags (`server -dir -s3 -s3.port -s3.config
  -ip.bind`), the identity-file format (`s3.dev.json`), and the healthcheck on the master's `/cluster/status`.
  Swapping the product means changing exactly these, plus the S3 check in `verify-dev-stack.sh`.
- **The rule for P1:** P1 code must use only the standard S3 API through a storage port/adapter, must not rely on
  the "folder" semantics listed above, and must run its storage integration tests against the store finally
  chosen for staging and production.

## 4. Development versus production object storage
The dev stub proves only that the platform can talk S3 locally. It says nothing about the production
requirements: durability, regional residency (OWNER + LEGAL), server-side encryption semantics (the platform
encrypts client-side under SECURITY section 3, so it **must not depend on store-side encryption**), backup and
retention, access policies, and the deletion guarantees behind crypto-shredding. Those are production decisions.

## 5. Alternatives considered

| Option | Licence / status | Fit for dev | Main drawback |
|---|---|---|---|
| **SeaweedFS** (proposed) | Apache-2.0, active | Good | S3 edge-case differences (section 2) |
| Garage | AGPL-3.0, active | Good | Needs a post-start bootstrap (layout + key creation); AGPL |
| LocalStack (S3) | Emulator; terms have changed over time | Good for API tests | Emulator, not a real store; licence and feature drift |
| Ceph RGW | LGPL, active | Poor (heavy) | Operational weight far beyond a dev stub |
| Archived MinIO image or a community fork | Unmaintained or unofficial | Works | Unpatched or unofficial supply chain |

## 6. Recommendation
**Approve SeaweedFS 4.54 as the development-only S3 stub** (ADR-0005 → *Accepted* once recorded in
DECISION_LOG), with two conditions:
1. P1 storage code stays on the standard S3 API behind a port, without SeaweedFS-specific behaviour.
2. The choice is revisited when the production store is selected.

**Consequence of waiting:** none for P1 planning. P0 cannot formally close while G0-3 is open, and P1 storage
integration work would be built on an unapproved stub.
