# `cia/` — isolated academic track (Track C) — NOT AUTHORISED

**This directory is an empty placeholder. Track C (C0–C3) is NOT authorised for implementation (D-21, clause 4).**

It is reserved for the isolated CIA modules described in ARCHITECTURE_OVERVIEW section 2 and CIA_REQUIREMENTS_MATRIX:
`cia-algorithms` (assessed, student-authored), `cia-bench` (benchmarks) and `cia-demo` (separately runnable demo).
None of them exists yet, and none is part of the Gradle build (`settings.gradle.kts` does not include `cia/`).

## Rules that apply now

- **No assessed algorithm material may be written here by an AI assistant**: no KMP, suffix array or LCP, Bloom
  filter or its hash function, heap, DSU or matching code, and none of their tests, baselines, benchmarks,
  correctness/complexity analysis, report or viva material. This holds until the faculty's AI-assistance policy
  is confirmed in writing **and** the owner records it in DECISION_LOG (D-20, D-21 clause 8).
- The platform must never depend on anything here. That is enforced mechanically:
  - Gradle: every platform project runs `verifyBoundaries`, which fails if any `cia` project or
    `com.thefineprint.cia` / `cia-*` artefact appears on its compile, runtime or test classpath
    (`build-logic/`, proven by TestKit tests).
  - ArchUnit: no `com.thefineprint.platform..` or `..core..` class may depend on `com.thefineprint.cia..`
    (`architecture-tests/`).
- Assessed implementations may depend only on `core-api` SPI types.

## Still missing (must not be invented)

CO1–CO5 definitions and the unit-to-CO mapping; actual submission and review dates; the faculty AI-assistance
policy; team composition and size; faculty approval of the slice's scope (CIA_REQUIREMENTS_MATRIX section 0).
