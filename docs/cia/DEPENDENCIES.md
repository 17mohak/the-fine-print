# Dependency manifest (graded slice)

Build files: [`build.gradle.kts`](../../build.gradle.kts), `cia/*/build.gradle.kts`, single version catalogue
[`gradle/libs.versions.toml`](../../gradle/libs.versions.toml). Every version is pinned exactly; resolved versions are
locked per module in `cia/*/gradle.lockfile` and every artefact's checksum is verified against
[`gradle/verification-metadata.xml`](../../gradle/verification-metadata.xml). Gradle itself is fetched by the wrapper
(9.9.2, checksum-verified). Nothing requires an account, API key or secret.

| Module | Runtime dependencies | Test / build-time only |
|---|---|---|
| `cia-algorithms` (assessed algorithms) | **none** beyond the JDK and the `core-api` module (enforced by the ArchUnit test `dependsOnlyOnTheJdkCoreAndCoreApi`) | JUnit Jupiter 6.1.3, AssertJ 3.27.7, ArchUnit 1.5.1 |
| `cia-demo` (backend + UI) | Spring Boot 4.2.0 (`spring-boot-starter-webmvc`: embedded Tomcat 11.0.27, Jackson, Logback), `cia-algorithms` | `spring-boot-starter-test` (JUnit 6.1.3, AssertJ, Mockito 5.24.0) |
| `cia-bench` (benchmarks) | `cia-algorithms`, JMH 1.37 (`jmh-core`, `jmh-generator-annprocess`) | JUnit, AssertJ |
| Frontend | none: plain HTML/CSS/JavaScript, no npm, no CDN, no third-party scripts | — |

Scripts (`ops/scripts/*.py`, `docs/cia/submission/make_figures.py`, `cia/cia-bench/scripts/make_charts.py`) use the
Python 3 standard library; only `make_figures.py` additionally needs `matplotlib`.

Full resolved lists: `cia/cia-algorithms/gradle.lockfile` (15 entries), `cia/cia-demo/gradle.lockfile` (78),
`cia/cia-bench/gradle.lockfile` (17). Supply-chain scan evidence (OSV, Trivy, gitleaks, CycloneDX SBOM task) is in
[`docs/evidence/p0`](../evidence/p0/security-scans.md); regenerate the SBOM with `./gradlew cyclonedxBom`.
