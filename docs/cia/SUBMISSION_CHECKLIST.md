# Submission checklist: CIA-3 deliverables and rubric → evidence

| Deliverable | Requirement | Status | Where |
|---|---|---|---|
| A. Proposal (1–2 pages) | title; team and IDs; problem; target user; features; two core techniques and why; data source; measurable success criteria | Done | submitted separately (document) |
| B. Repository | Java backend, frontend, tests, sample data, build files, dependency manifest, readable history, attribution | Done | [README](../../README.md) section 2, [DEPENDENCIES.md](DEPENDENCIES.md), [CONTRIBUTIONS.md](CONTRIBUTIONS.md) |
| C. Technical report (5–8 pages) | problem and constraints; design/API diagram; pseudocode; correctness; complexity; benchmark method and results; limitations; individual contributions | Done | submitted separately (document) |
| D. Demonstration evidence | working app; example inputs/outputs; error handling; performance charts/tables | Done | [evidence/README.md](evidence/README.md) |
| E. README and reproducibility | prerequisites; build/run commands; env vars without secrets; API | Done | [README](../../README.md) sections 3–7 and 11 |

## Guide requirements → evidence

| Requirement | Evidence |
|---|---|
| ≥ 2 distinct syllabus techniques, ≥ 1 from Units 4–6 | KMP (Unit 4) and Bloom filter (Unit 6) |
| CO5: randomised / probabilistic structure | Bloom filter ([A2](A2_BLOOM_FILTER.md)), measured false-positive rate vs theory, exact confirmation |
| Java for all core algorithms and business logic, run server-side | `cia-algorithms`, `cia-demo`; the UI only calls `/algo/*` (`app.js`, `DemoHttpTest`) |
| Original implementations, no library doing the algorithm | ArchUnit `AssessedCodeIsolationTest` (no JDK search, set or bit-set classes), with a violating fixture proving the rule fires |
| ≥ 5 correctness tests per algorithm incl. a boundary test | KMP 17 (`KmpMatcherTest`, `KmpQuoteVerifierTest`); Bloom 31 (`BloomFilterTest`, `ProbeSequenceTest`, `BitArrayAndHashTest`); see the [test report](evidence/test-report.md) |
| Benchmarks at ≥ 3 sizes with ≥ 1 baseline, reproducible | 10⁴ / 10⁵ / 10⁶; naive and `String.indexOf`; `HashSet<Long>`; commands in README section 9 |
| Documented synthetic data, assumptions, failure cases | [DATA.md](DATA.md), `data/samples` |
| Validation: empty, invalid, duplicates, infeasible, edge cases; no silent wrong results | [evidence section 3](evidence/README.md), 400 problem responses, `MISALIGNED` / `AMBIGUOUS`, duplicate accounting |
| Honesty about probabilistic output | UI and report: "possibly present" is always confirmed exactly |
| Viva readiness | [VIVA.md](VIVA.md), [DEMO_SCRIPT.md](DEMO_SCRIPT.md) |

| Rubric item (marks) | Evidence |
|---|---|
| Problem formulation (10) | Proposal sections 2–3, report section 1 |
| Algorithm selection + original implementation (20) | Proposal section 4, report section 3, [A1](A1_KMP.md), [A2](A2_BLOOM_FILTER.md), source |
| Correctness + tests (20) | Report section 4, test report |
| Complexity + empirical performance (15) | Report sections 5–6, [BENCHMARKS.md](BENCHMARKS.md) |
| Java backend / API quality (10) | `cia-demo`, report section 2, README section 6 |
| Frontend integration / usability (5) | `static/`, screenshots |
| Documentation / reproducibility (5) | README, runbook, this checklist |
| Demo + viva (15) | [DEMO_SCRIPT.md](DEMO_SCRIPT.md), [VIVA.md](VIVA.md) |

## Still to do by the student before submitting
1. Read and, if necessary, edit [CONTRIBUTIONS.md](CONTRIBUTIONS.md) so it states exactly what you did; use the faculty's disclosure wording if one exists.
2. Attach the written faculty confirmation of the AI-assistance policy if you have it (DECISION_LOG D-31 notes that none is held in the repository).
3. Check that the repository's default branch shows this work (merge the pull request) and that the link opens when you are logged out.
