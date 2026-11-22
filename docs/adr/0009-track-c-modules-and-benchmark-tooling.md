# ADR-0009: Track C modules, benchmark tooling and demo stack

- **Status:** Accepted (engineering) — under the Track C authorisation D-31; for owner and reviewer review on the PR
- **Date:** 2026-11-20
- **Phase / authorisation:** Track C, C1–C2 (DECISION_LOG D-31)
- **Decision owner:** Engineering, within approved architecture (ARCHITECTURE_OVERVIEW section 2; handover section 8)
- **Related:** ADR-0003 (boundary enforcement), ADR-0004 (core-api SPIs), ADR-0007 (supply chain), CIA_REQUIREMENTS_MATRIX

## Context
D-31 authorises the assessed algorithms in isolated `cia-*` modules. The architecture already names them:
`cia-algorithms` (depends only on `core-api`), `cia-bench` (JMH) and `cia-demo` (a runnable app with no database
server, network, LLM or worker).

## Decision
1. **Modules:** `:cia:cia-algorithms`, `:cia:cia-bench`, `:cia:cia-demo`, all on `fineprint.java-conventions`:
   Java 21, `-Werror`, STRICT locking. The `verifyBoundaries` task is not registered for `:cia` projects, by
   design. The platform side still fails if it ever resolves one.
2. **Isolation inside `cia-algorithms`:** an ArchUnit test allows only `java.lang..`, `java.util`,
   `java.util.stream` and `core-api`, and forbids `BitSet`, `HashSet`, `HashMap`, `TreeSet`, `TreeMap`,
   `LinkedHashSet`, `PriorityQueue` and JDK `String` search methods. A cheating fixture proves the rules fire.
3. **Benchmarks:** JMH 1.37 (latest release; TECHNOLOGY_DECISIONS #20 lists JMH), used through
   `jmh-generator-annprocess` and a plain `JavaExec` task, with no Gradle JMH plugin. It is opt-in
   (`./gradlew :cia:cia-bench:jmh`) and **not part of `build` or CI** (it takes about 15 minutes). A harness
   smoke test runs with `build`. Results are committed under `cia/cia-bench/results/`. JMH is GPLv2 with the
   Classpath Exception and is used only in the benchmark module, never shipped.
4. **FPR and memory experiment:** a deterministic Java program (`BloomExperiment`). HashSet memory is measured
   approximately from the used-heap delta after forced GCs; JOL was not added, to avoid another dependency.
   The figure is labelled approximate.
5. **Charts:** a dependency-free Python script writes SVG from the committed JSON and CSV.
6. **Demo:** Spring Boot 4.2.0 (`webmvc` only) on `127.0.0.1:8090`; stateless; `/algo/quote-match` and
   `/algo/membership`; RFC 9457 errors without input echo; bounded input sizes. The UI is static HTML, CSS and
   JS served by the same app: no Node toolchain, no frontend dependencies, all DOM built with `textContent`.

## Options considered

| Option | Why not chosen |
|---|---|
| `me.champeau.jmh` Gradle plugin | Another build plugin; a `JavaExec` task is enough and easier to explain |
| jqwik for property tests | Seeded randomized JUnit tests meet the requirement without a dependency |
| React/Vite for the demo UI | A toolchain for a two-panel demo; the product UI (later phase) is where it belongs |
| JOL for memory | Exact, but another dependency with JVM-attach caveats; the approximation is documented instead |

## Consequences
- New third-party artefacts: `jmh-core`, `jmh-generator-annprocess`, `jopt-simple`, `commons-math3`
  (bench-only). They are added to the locks and the verification metadata, which was regenerated from an empty
  cache.
- CI runs all `cia-*` tests (through `build`) but no JMH.

## Deviation from the approved architecture?
No. The modules and their roles are those named in ARCHITECTURE_OVERVIEW section 2 and the handover.
