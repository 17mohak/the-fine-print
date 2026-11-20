# P0 verification record

- **Date:** 2026-11-20
- **Branch / commit verified:** `p0/scaffolding` @ `e1ea7b1`
- **Environment:** Windows 11 (10.0.26200); Oracle JDK 21.0.4; Gradle 9.9.2 (wrapper); Git 2.46; Docker client 29.2.0 /
  Compose v5.0.2 with **the Docker engine not running**
- **Authorisation:** Track P, P0 only (D-21). Owner instruction for this session: proceed without Docker Desktop
  and mark container-dependent checks as blocked.

> **P0 acceptance is NOT complete.** Criteria 4 and 5, the containerised scans in criterion 8, and CI execution
> are outstanding. Status below is reported exactly as observed.

## Acceptance checklist (IMPLEMENTATION_ROADMAP P0 and the P0 directive)

| # | Criterion | Status | Evidence |
|---|---|---|---|
| 1 | Install prerequisites and follow documented setup | **PASS (partial)**: JVM path only | [local-setup.md](../../runbooks/local-setup.md), steps 1–3 followed in a fresh clone. Docker steps 5–7 not exercised |
| 2 | Build the Gradle project | **PASS** | Fresh clone + empty `GRADLE_USER_HOME`: `./gradlew build verifyBoundaries cyclonedxBom`, giving `BUILD SUCCESSFUL in 2m 58s`, 111/111 tasks executed |
| 3 | Run the available automated checks | **PASS** (JVM checks) | 36 tests, 0 failures, 0 errors, 0 skipped (breakdown below); `verifyBoundaries` OK in all 23 Java projects |
| 4 | Start the development Compose environment | **BLOCKED / NOT RUN — DOCKER ENGINE UNAVAILABLE** | Only static validation: `docker compose … config --quiet` exits 0, and a missing `.env` fails loudly as designed |
| 5 | Verify the expected services and stubs | **BLOCKED / NOT RUN — DOCKER ENGINE UNAVAILABLE** | `ops/scripts/verify-dev-stack.sh` written; only its shell syntax was checked (`bash -n`) |
| 6 | Architecture tests enforce module boundaries | **PASS** | `PlatformArchitectureTest` (8) over the real classes, plus `ArchitectureFixturesTest` (6) proving each rule catches a violation |
| 7 | `cia-*` absent from the platform runtime classpath | **PASS** | `verifyBoundaries` (resolved graph, 4 classpaths) in every project; TestKit proofs that direct, transitive and test-scope cia dependencies fail; [platform-runtime-classpath.txt](platform-runtime-classpath.txt) has no cia coordinate; no cia component in the SBOM |
| 8a | Generate the SBOM | **PASS** | `build/reports/cyclonedx/bom.{json,xml}`: CycloneDX 1.6, 74 runtime components, 0 cia |
| 8b | Dependency vulnerability scan | **BLOCKED / NOT RUN — DOCKER ENGINE UNAVAILABLE** (and CI not executed) | OSV-Scanner is configured in CI job `dependency-scan`; it has not been run anywhere |
| 8c | Secret scan | **BLOCKED / NOT RUN — DOCKER ENGINE UNAVAILABLE** (and CI not executed) | gitleaks configured in CI job `secret-scan`. A manual grep sweep found no secrets (below); that is **not** a substitute |
| — | CI pipeline runs green | **NOT RUN**: no remote | `.github/workflows/ci.yml` parses as YAML (5 jobs); never executed |
| — | Commit-attribution rules | **PASS** | `check-commit-attribution.sh HEAD` passes on all commits; hook and CI check tested in a scratch repo (an AI trailer was rejected; a hook-bypassed AI trailer was caught by the check) |

## Test breakdown (fresh clone run)

| Suite | Tests | Result |
|---|---|---|
| `build.BoundaryPolicyTest` | 5 | pass |
| `build.BoundariesPluginFunctionalTest` (TestKit) | 7 | pass |
| `core.api.CoreApiTypesTest` | 3 | pass |
| `api.OpenApiSpecTest` | 4 | pass |
| `architecture.PlatformArchitectureTest` | 8 | pass |
| `architecture.ArchitectureFixturesTest` | 6 | pass |
| `platform.app.FinePrintApplicationTest` (health UP; only health exposed; no business endpoints) | 3 | pass |
| **Total** | **36** | **36 pass** |

## Commands executed (key ones) and results

| Command | Result |
|---|---|
| `git init -b main`; import commit of the 39 existing files; `git switch -c p0/scaffolding` | OK. The governing documents are byte-identical to the originals (`core.autocrlf=false` set locally; `.gitattributes` leaves `*.md` untouched) |
| `./gradlew resolveAndLockAll --write-locks` | 25 lockfiles written. A fresh-clone build reproduces them exactly |
| `./gradlew --write-verification-metadata sha256 …` | 334 components with SHA-256 (after the clean-cache completion in `e1ea7b1`) |
| `./gradlew build` (working copy) | BUILD SUCCESSFUL |
| `./gradlew cyclonedxBom` | SBOM generated |
| `docker compose -f ops/compose/compose.yaml --env-file ops/compose/.env.example config --quiet` | exit 0 (static validation only) |
| Fresh clone into a path containing spaces, empty `GRADLE_USER_HOME`, `./gradlew build verifyBoundaries cyclonedxBom --no-daemon` | **BUILD SUCCESSFUL**; working tree still clean afterwards |
| `bash ops/scripts/check-commit-attribution.sh HEAD` | passed (all commits) |
| Secret sweep: grep for password/secret/key/token/private-key/AKIA patterns outside the allow-listed dev files | only `${VAR}` references in compose.yaml and a Javadoc "must not store passwords"; no secrets |

## Defects found and fixed during verification (honest record)

1. **Build-logic sources were not committed.** The `.gitignore` pattern `build/` also matched the source package
   `com/thefineprint/build`. The working copy built, but a fresh clone could not. Found by the clean-checkout
   test and fixed in `1a3947f` (re-include `**/src/**/build/`).
2. **Dependency verification metadata was incomplete.** Parent POMs already in the local Gradle cache were not
   recorded, so a clean machine failed verification. Regenerated from an empty cache in a fresh clone (additions
   only) in `e1ea7b1`.
3. Minor: the OpenAPI test failed on paths containing spaces. It now uses a file URI.

## Environmental and other limitations

- **Docker engine unavailable** (owner chose not to restart). Nothing container-based has run: the Compose stack,
  service healthchecks (the SeaweedFS `wget` probe, the Keycloak `/dev/tcp` probe, SeaweedFS command flags),
  `verify-dev-stack.sh`, gitleaks, OSV-Scanner, and the `docker run` scan commands in the runbook are all
  **unverified**.
- **CI has never executed.** No git remote exists, and creating one was not authorised. Workflow correctness
  (action inputs, the gitleaks container invocation, OSV-Scanner arguments) is unverified.
- **"Clean machine" was approximated.** I used a fresh clone plus an empty Gradle home on the same Windows host
  with the same JDK 21.0.4. Linux, macOS and a different JDK vendor have not been tested (CI on Ubuntu with
  Temurin 21 would cover Linux).
- The local JDK 21.0.4 (July 2024) lacks recent security patches. Updating it is recommended.
- The ArchUnit provider-SDK list is a deny-list; it must be kept current when a provider is chosen (P2).
- The commit-attribution pattern does not catch a bare "Claude" trailer with a non-Anthropic e-mail
  (ADR-0008 residual gap).
- **No security claim.** The dev stack is loopback-only and synthetic, and that is all. Nothing here is secure,
  production-ready or DPDP-compliant.

## Outstanding owner decisions (for G0)

1. **Exact versions** (TECHNOLOGY_DECISIONS #2): confirm [ADR-0002](../../adr/0002-p0-toolchain-and-versions.md),
   notably Spring Boot **4.2.0** (GA one day before this record) versus 4.1.3.
2. **D-20 path**: the faculty AI-assistance policy is still OPEN. No Track C work happens until it is supplied
   in writing and recorded.
3. **MinIO substitute** ([ADR-0005](../../adr/0005-dev-object-store-minio-unavailable.md), a deviation): MinIO's
   Docker Hub repository returns 404 and its GitHub repository is archived. SeaweedFS is proposed for
   development only.
4. **CI remote**: whether to create a (private) GitHub repository so CI can run.
5. **G0-SEC**: engage a security specialist before P1 (RESEARCH_BACKLOG A-13).

## Minor inconsistencies noticed in the governing documents (not edited)

- CIA_REQUIREMENTS_MATRIX section 5 refers to `algo-bench`; elsewhere the module is `cia-bench`.
- RESEARCH_BACKLOG B-11 cites "SYSTEM_DESIGN 2.6" for the fixtures; the test catalogue is section 2.7.
- CIA_REQUIREMENTS_MATRIX section 0 would allow `cia-demo` scaffolding and stub classes after approval, but D-21
  restricts `cia/` to placeholders. D-21 was followed.

## Remaining to complete P0 acceptance (next session, with Docker running)

1. Start Docker Desktop.
2. Bring the stack up:
   - `cp ops/compose/.env.example ops/compose/.env`
   - `docker compose -f ops/compose/compose.yaml --env-file ops/compose/.env up -d --wait`
3. Run `bash ops/scripts/verify-dev-stack.sh` and fix any healthcheck or flag issues.
4. Run gitleaks and OSV-Scanner (runbook section 7) and record the results.
5. Optionally push to an owner-approved remote and confirm all five CI jobs are green.
6. Update this record. Declare P0 complete only when every row above is PASS.

## Scope confirmation

No P1 or later work was started. No encryption, key management, RLS, tenant context, worker or broker,
canonicalisation, provenance, search index, production SPI adapter, LLM integration or domain logic exists.
`cia/` contains only `README.md`. No assessed algorithm code, tests, benchmarks or analysis were written. No real
or personal data is in the repository.
