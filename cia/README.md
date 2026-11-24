# `cia/` — isolated academic track (Track C)

**Status:** Track C (C1–C2) is **authorised** by DECISION_LOG **D-31** (2026-11-20, owner decision). It remains
**isolated** from the production platform.

| Module | Purpose | May depend on |
|---|---|---|
| `cia-algorithms` | Assessed algorithm implementations: A1 KMP quote matching (Unit 4), A2 Bloom filter (Unit 6 / CO5), A3 disjoint-set forest for document families (Unit 2) | `core-api` and the JDK only |
| `cia-bench` | Reproducible JMH benchmarks and false-positive-rate/memory experiments | `cia-algorithms`, JMH |
| `cia-demo` | Runnable Spring Boot demonstration with a browser UI; `/algo/*` endpoints exist only here | `cia-algorithms`, Spring Boot |

## Boundaries (enforced, not just documented)
- The platform must never depend on anything here. Every platform project runs `verifyBoundaries`, which fails
  if a `cia` project or a `cia-*` / `com.thefineprint.cia` artefact appears on any of its classpaths. ArchUnit
  checks the same rule at class level.
- `cia-algorithms` contains no Spring, database or network code, and its assessed classes do not delegate to JDK
  search, set, bit-set or regex utilities. Both rules are tested in `cia-algorithms`.
- Production use of an assessed algorithm requires the seven-point promotion process and a separate architecture
  review (ARCHITECTURE_OVERVIEW section 2). The default is no promotion.

## Documentation
- Algorithms: [docs/cia/A1_KMP.md](../docs/cia/A1_KMP.md), [docs/cia/A2_BLOOM_FILTER.md](../docs/cia/A2_BLOOM_FILTER.md), [docs/cia/A3_DISJOINT_SET.md](../docs/cia/A3_DISJOINT_SET.md)
- Benchmarks: [docs/cia/BENCHMARKS.md](../docs/cia/BENCHMARKS.md)
- Viva preparation: [docs/cia/VIVA.md](../docs/cia/VIVA.md)

## Still missing (do not invent)
CO1–CO5 definitions and the unit-to-CO mapping, actual dates, the full syllabus, and the required AI-assistance
disclosure form (D-31, "Still open").
