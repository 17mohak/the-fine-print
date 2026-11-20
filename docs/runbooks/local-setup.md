# Local setup (P0)

Reproducible from a clean checkout. Commands assume a POSIX shell (Linux, macOS, or Git Bash on Windows). On
Windows PowerShell, use `.\gradlew.bat` in place of `./gradlew`.

## 1. Prerequisites

| Tool | Version | Needed for |
|---|---|---|
| JDK | 21 (any 21.x; Temurin recommended, ideally a current patch) | Build. Gradle runs on it, and the toolchain must find a JDK 21 |
| Git | 2.40+ | Checkout |
| Docker Engine + Compose v2 | Docker 24+ / Compose 2.20+ (needs `--wait`) | Dev stack only (steps 5–6) |
| curl, bash | any | `verify-dev-stack.sh` |

You do **not** need a separate Gradle install: the wrapper downloads Gradle 9.9.2 and verifies its SHA-256.
The first build needs internet access to `services.gradle.org`, `plugins.gradle.org` and Maven Central.

## 2. Clone

```bash
git clone <repository-url> fineprint
cd fineprint
git config core.hooksPath ops/git-hooks   # optional: local commit-attribution hook
```

## 3. Build and run all JVM checks (no Docker needed)

```bash
./gradlew build
```

This compiles every module and runs every test: unit tests, the OpenAPI validation, the ArchUnit boundary rules,
the boundary-plugin TestKit tests, `verifyBoundaries` in every module, and the app health test. Dependencies
are checked against `gradle/verification-metadata.xml`, and locked versions against `gradle.lockfile`s.

Individual checks:

```bash
./gradlew verifyBoundaries                      # Gradle boundary check in every module
./gradlew :architecture-tests:test              # ArchUnit rules + fixture proofs
./gradlew :platform:app:dependencies --configuration runtimeClasspath   # inspect platform runtime classpath
./gradlew cyclonedxBom                          # SBOM -> build/reports/cyclonedx/bom.json
bash ops/scripts/check-commit-attribution.sh    # commit-attribution rules
```

## 4. Run the application (optional)

```bash
./gradlew :platform:app:bootRun
curl http://127.0.0.1:8080/actuator/health      # {"status":"UP"}
```

It listens on loopback only and exposes only the health endpoint. It has no database or IdP wiring in P0.

## 5. Start the development stack (Docker)

```bash
cp ops/compose/.env.example ops/compose/.env
docker compose -f ops/compose/compose.yaml --env-file ops/compose/.env up -d --wait
```

| Service | Address (loopback only) | Notes |
|---|---|---|
| PostgreSQL 18.7 | `127.0.0.1:5432` | empty database `fineprint_dev`; no schema or RLS (P1) |
| Object store (SeaweedFS S3, ADR-0005) | `http://127.0.0.1:8333` | dev-only key pair in `ops/compose/seaweedfs/s3.dev.json` |
| Dev IdP (Keycloak 26.8.2) | `http://127.0.0.1:8081` | realm `fineprint-dev`; synthetic user and admin bootstrap values in `realm.json` / `.env.example` |
| doc-worker-stub | (no network) | inert: prints a banner and sleeps |

All values in these files are labelled development-only and protect synthetic data only. **Never put real
documents or personal data into this stack.**

## 6. Verify the stack

```bash
bash ops/scripts/verify-dev-stack.sh
```

Stop and delete the stack's data:

```bash
docker compose -f ops/compose/compose.yaml --env-file ops/compose/.env down -v
```

## 7. Supply-chain scans (container-based; also run in CI)

```bash
docker run --rm -v "$PWD:/repo" -w /repo zricethezav/gitleaks:v8.30.1 git --config /repo/.gitleaks.toml --redact /repo
docker run --rm -v "$PWD:/src" ghcr.io/google/osv-scanner-action:v2.7.1 --recursive /src
```

## Updating dependencies

1. Change the version in `gradle/libs.versions.toml`.
2. Update the locks: `./gradlew resolveAndLockAll --write-locks`
3. Update the checksums: `./gradlew --write-verification-metadata sha256 build cyclonedxBom`
4. Review the diff, add or adjust an ADR if the change is significant, then commit.
