# ADR-0003: Module layout and mechanical boundary enforcement

- **Status:** Accepted (engineering) — owner review at G0
- **Date:** 2026-11-20
- **Phase / authorisation:** Track P, P0 (D-21)
- **Decision owner:** Engineering (within approved module boundaries)
- **Related:** OPUS_BUILD_HANDOVER sections 7–8; ARCHITECTURE_OVERVIEW sections 2, 4, 5; IMPLEMENTATION_ROADMAP P0 acceptance

## Context
P0 acceptance requires mechanical enforcement of three rules:
- (a) `platform/*` must not depend on `cia-*`;
- (b) domain modules don't depend on each other;
- (c) only `modelgateway` calls providers.

It also requires a Gradle check proving `cia-*` is absent from the platform runtime classpath. D-21 permits only
empty placeholders for `cia/`, so no `cia-*` module may exist. A check against an empty set would pass without
proving anything.

## Decision
1. **Layout** follows handover section 8, without the outer `fineprint/` folder (the repository root is the
   project root, so the governing documents keep their paths):
   - `core-api`;
   - `platform/{app, identity-access, crypto, documents, workerbroker, canonical, provenance, knowledge, understand, modelgateway, commitments, cases, integrations, audit}`;
   - `platform/domains/{generic-agreement, lending, insurance, tenancy, subscriptions, warranties}`;
   - `api` (OpenAPI);
   - `architecture-tests`;
   - `build-logic` (included build);
   - placeholders `cia/`, `doc-worker/`, `web/`, `db/migrations/`, `data/`.

   Base package: `com.thefineprint` (routine naming choice; changeable before P1 at low cost).
2. **Placeholders.** Each platform module except `app` contains only a `package-info.java` that states its
   responsibility, its "must not" and its status. `:platform:app` depends on every platform module, so its
   runtime classpath *is* the platform runtime classpath.
3. **Gradle enforcement (`build-logic`, plugin `fineprint.boundaries`).** Every Java project outside `:cia` gets
   `verifyBoundaries`, wired into `check`. It walks the **resolved** graphs of `compileClasspath`,
   `runtimeClasspath`, `testCompileClasspath` and `testRuntimeClasspath`, so transitive paths count, and fails if:
   - any `cia` project, `com.thefineprint.cia*` group or `cia-*` artefact appears; or
   - a `:platform:domains:X` project reaches another domain module.

   **Proof the check works:** Gradle TestKit tests build throwaway projects (including a temporary
   `:cia:cia-placeholder` that exists only inside the test) and assert that direct, transitive and test-scope
   violations fail.
4. **ArchUnit enforcement (`architecture-tests`)** over the real classes of `core-api` and all platform modules:
   - (a) no platform/core class depends on `com.thefineprint.cia..`;
   - (b) domain slices do not depend on each other;
   - (c) provider-SDK packages are used only in `platform.modelgateway`;
   - (c, continued) outbound network clients (`java.net.http`, Spring `RestClient`/`WebClient`, OkHttp,
     Apache HttpClient, `HttpURLConnection`/`URLConnection`/`Socket` …) are used only in `modelgateway` and
     `integrations`, which also covers "cases cannot call external systems";
   - `core-api` depends only on `java..`;
   - no untrusted-file parser (PDFBox, iText/OpenPDF, Tika, POI, Tess4J, `javax.imageio`) anywhere in the Java
     core (ARCH section 4; D-23).

   Rules may match nothing in empty placeholder modules, so a guard test asserts the import is non-empty, and
   fixture tests (`fixtures.violating` / `fixtures.compliant`) prove each rule detects a violation.
5. `settings.gradle.kts` does not include `cia/`. CI also greps the resolved runtime classpath of `:platform:app`
   for any `cia` coordinate.

## Options considered
| Option | Pros | Cons |
|---|---|---|
| Resolved-graph Gradle task + ArchUnit (chosen) | Catches transitive, test-scope and bytecode-level violations; proven by negative tests | Custom build logic to maintain (~150 lines) |
| Declared-dependency checks only | Simple | Misses transitive violations |
| Create an empty `cia-algorithms` module to test against | Realistic | Violates D-21 (placeholders only in `cia/`) |
| Java modules (JPMS) | Compiler-enforced | Heavy friction with Spring; not in the approved stack |

## Consequences
- A new module automatically gets `verifyBoundaries` by applying `fineprint.java-conventions`.
- The provider-SDK list is a deny-list and must grow when a provider is chosen (P2). Missing an SDK package is a
  residual risk, which code review mitigates.
- Not covered yet, because the code it would constrain does not exist: "only workerbroker talks to the worker"
  (P1) and the worker's own Python boundaries.

## Reversibility and revisit triggers
High. Revisit when P1 adds real module code, or if the build adopts isolated projects or the configuration cache.

## Deviation from the approved architecture?
No. The layout drops only the nominal outer `fineprint/` folder.
