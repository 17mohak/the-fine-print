# P0 verification record

- **Revision 2:** 2026-11-20, Docker-dependent verification and first CI runs. Revision 1 (Docker unavailable) is in
  git history at `93939ae`.
- **Repository:** https://github.com/17mohak/the-fine-print (**private**; sole collaborator `17mohak`)
- **Branch / commit verified:** `p0/scaffolding` (`main` = `1e8af08`, the document import only)
- **Environment:** Windows 11 (10.0.26200); Oracle JDK 21.0.4; Gradle 9.9.2 (wrapper); Git 2.46; Docker Engine
  29.2.0 / Compose 5.0.2. CI: GitHub-hosted `ubuntu-24.04`, Temurin 21.
- **Authorisation:** Track P, P0 only (D-21). The owner approved the private repository and the Docker
  verification at G0 on 2026-11-20.

> **Status:** every P0 acceptance criterion has now been demonstrated, locally and on GitHub Actions; the CI
> results are linked below. **P0 still closes only at gate G0, with owner confirmation of the decisions listed
> at the end.** Nothing here is a claim that the system is secure or production-ready.

## Acceptance checklist

| # | Criterion | Status | Evidence |
|---|---|---|---|
| 1 | Install prerequisites and follow the documented setup | **PASS** | [local-setup.md](../../runbooks/local-setup.md) steps 1–7 executed. Runbook corrected for the port-override and clean-cache findings |
| 2 | Build the Gradle project | **PASS** | Fresh clone + empty `GRADLE_USER_HOME` (rev. 1), regeneration clone (rev. 2), and the CI `build` job on Linux |
| 3 | Run the available automated checks | **PASS** | 36 tests, 0 failures; `verifyBoundaries` in 23 projects; locally and in CI |
| 4 | Start the development Compose environment | **PASS** | `up -d --wait`: 4/4 services healthy (local, and the CI `compose-smoke` job) |
| 5 | Verify the expected services and stubs | **PASS** | `verify-dev-stack.sh` 11/11 PASS ([initial](dev-stack-verify-initial.txt)); detailed [inspection](dev-stack-inspection.txt); [lifecycle](dev-stack-lifecycle.txt) |
| 6 | Architecture tests enforce module boundaries | **PASS** | ArchUnit 8 rules over real classes + 6 fixture proofs (local and CI) |
| 7 | `cia-*` absent from the platform runtime classpath | **PASS** | `verifyBoundaries` + TestKit proofs; CI classpath grep step; [platform-runtime-classpath.txt](platform-runtime-classpath.txt); SBOM has 0 cia components |
| 8a | SBOM generated | **PASS** | CycloneDX 1.6 aggregate; CI artefact `sbom-cyclonedx` |
| 8b | Dependency scan | **PASS after fix** | 2 initial findings, fixed or justified ([security-scans.md](security-scans.md)); now "No issues found" |
| 8c | Secret scan | **PASS** | gitleaks: no leaks in full history, with and without the allow-list |
| — | CI runs green remotely | **PASS** | Run [44666164953](https://github.com/17mohak/the-fine-print/actions/runs/44666164953): all 5 jobs green (details below) |
| — | Commit attribution | **PASS** | All commits: author = committer = `Mohak Mandwani <mohak2k6@gmail.com>`, linked to GitHub user `17mohak`; 0 `Co-authored-by` trailers; checked locally, through the GitHub API, and by CI job `attribution` |

## Docker service verification (local, 2026-11-20)

| Check | Result |
|---|---|
| Pinned images | Postgres, SeaweedFS, Keycloak and busybox pulled; each local RepoDigest equals the digest in `compose.yaml` |
| Health | All four `healthy` (first start about 160 s, mostly Keycloak) |
| PostgreSQL 18.7 | Accepts connections; 0 user tables, 0 RLS policies, 0 RLS-enabled tables; only role `fineprint_dev` |
| SeaweedFS S3 (ADR-0005, *still Proposed*) | Anonymous request 403; wrong secret 403; dev identity ListBuckets 200 with 0 buckets |
| Keycloak 26.8.2 | OIDC discovery for `fineprint-dev`; password grant refused (`unauthorized_client`); PKCE `plain` and missing PKCE rejected (S256 enforced); management port 9000 not reachable from the host |
| Worker stub | `NetworkMode=none`, only `lo`, egress "Network unreachable", read-only root, uid/gid 65534, `CapEff=0`, `no-new-privileges`, pids 16, memory 32 MiB; logs state it is an inert P0 stub; no processing code |
| Port bindings | 127.0.0.1 only (15432, 8333, 18081 locally; 5432, 8333, 8081 in CI). SeaweedFS internal ports 9333/8080/8888/18080/19333 not reachable from the host |
| Network scope | Only the worker stub is network-isolated. The other services use the default non-internal bridge network; that is as designed for P0 dev stubs |
| Lifecycle | `restart` → 11/11; `down` (volumes kept, probe database persisted) → `up` → 11/11; `down -v` (volumes removed, probe gone) → `up` → 11/11; a killed worker stub stays exited (no restart policy) until `up` |

The synthetic persistence probe was an empty database named `p0_persistence_probe`. It was removed by `down -v`.

## Security scans

Details in [security-scans.md](security-scans.md); raw output in [scan-gitleaks.txt](scan-gitleaks.txt) and
[scan-osv.txt](scan-osv.txt).
- **gitleaks:** no leaks, both locally and in CI.
- **OSV-Scanner:**
  - Guava 33.7.1 (High, build-time plugin classpath only): **fixed** by moving to 33.7.2.
  - commons-lang3 3.16.0 (Medium): **false positive**, never resolved; covered by an expiring override.
  - Result: no issues.

## GitHub Actions

Workflow `.github/workflows/ci.yml`:
- triggers on `push` to `main` and `p0/**`, and on `pull_request`;
- top-level `permissions: contents: read`;
- 10/10 `uses:` pinned to full commit SHAs;
- gitleaks image pinned by digest;
- no secrets used;
- no warning annotations on run 1.

| Run | Commit | Conclusion | Jobs |
|---|---|---|---|
| [44663133885](https://github.com/17mohak/the-fine-print/actions/runs/44663133885) | `93939ae` | **failure** (expected; it surfaced the OSV findings) | build ✅ · attribution ✅ · secret-scan ✅ · compose-smoke ✅ · dependency-scan ❌ (2 advisories) |
| [44666164953](https://github.com/17mohak/the-fine-print/actions/runs/44666164953) | `a401007` | **success** | build ✅ (BUILD SUCCESSFUL; classpath step passed; SBOM and test-report artefacts uploaded) · attribution ✅ (10 commits) · secret-scan ✅ (no leaks) · compose-smoke ✅ (11/11 PASS) · dependency-scan ✅ ("No issues found", 1 filtered) |
| Run for the commit that adds this record | see the GitHub Actions tab; its conclusion is reported in the session handover, not in this file (it cannot contain its own result) | | |

The `pull_request` trigger is verified from configuration only. No pull request has been opened.

## Defects found and fixed in revision 2

1. `verify-dev-stack.sh` ignored host-port overrides. It now reads `IDP_PORT`/`S3_PORT` from the env file (`99de45c`).
2. Guava 33.7.1 (GHSA-xxph-c9ww-hj94) on the build's plugin classpath. Constrained to 33.7.2, the plugin classpath
   is now locked, and the verification metadata was regenerated from an empty cache (`a401007`).
3. The commons-lang3 OSV false positive is handled by an exact-version, expiring override, passed to CI with
   `--config` (`a401007`).
4. The build-logic test classpath was not locked. It is now (`a401007`).

Revision-1 defects (unchanged): the `.gitignore` that hid the build-logic sources (`1a3947f`); verification
metadata incomplete on clean machines (`e1ea7b1`).

## Remaining limitations

- Local JDK 21.0.4 (July 2024) is missing security patches. CI uses the current Temurin 21.
- macOS has not been tested. Linux is covered by CI only.
- The OSV action pulls its scanner image by tag (the action itself is SHA-pinned).
- The ArchUnit provider-SDK list is a deny-list (ADR-0003). The bare-"Claude" trailer gap is unchanged (ADR-0008).
- The SBOM's "disconnected graph" notices for the folder-only parent projects are cosmetic.
- **No security claim.** G0-SEC is still required before P1.

## Outstanding G0 decisions (owner)

1. **Exact versions** ([ADR-0002](../../adr/0002-p0-toolchain-and-versions.md)), not selected on your behalf:
   - Java 21 (`--release 21`; local Oracle 21.0.4, CI Temurin 21)
   - Gradle 9.9.2
   - **Spring Boot 4.2.0** (GA 2026-11-19), with **4.1.3** as the more mature alternative
   - JUnit 6.1.3, AssertJ 3.27.7, ArchUnit 1.5.1, swagger-parser 2.1.49, CycloneDX plugin 3.6.1
   - PostgreSQL 18.7 (19 is RC only), Keycloak 26.8.2, SeaweedFS 4.54, busybox 1.37.0
   - gitleaks 8.30.1, OSV-Scanner 2.7.1
   - Build-time Guava 33.7.2-jre (security constraint)
2. **D-20 faculty AI-assistance policy:** OPEN. Not treated as resolved; no Track C work.
3. **MinIO → SeaweedFS** (ADR-0005, deviation): still Proposed.
4. **G0-SEC:** security-specialist engagement before P1 (RESEARCH_BACKLOG A-13).
5. Whether to open a `p0/scaffolding` → `main` pull request, which would also exercise the `pull_request` trigger.

## Scope confirmation

No P1 or Track C work was started. `cia/` contains only `README.md`. No assessed algorithm material exists. No
real or personal data was used; the only data is a synthetic user and an empty probe database.
