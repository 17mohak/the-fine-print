# CIA_REQUIREMENTS_MATRIX.md

Status: PROPOSED v0.1 · 2026-11-20. Source: **`CIA3_Advanced_Algorithm_Project_Guidelines_2026-27.pdf`** (ATLAS–uGDX, School of Technology, B.Tech CS AI&ML, Semester V; 5 pages; read in full this session). Nothing below is invented syllabus; items the guide does not state are listed in §1.

## 1. What the source does and does not provide
**VERIFIED FACT (from the guide):**
- Group of 2–3 (individual only with faculty approval); Java mandatory for "all core algorithms and business logic" (Spring Boot recommended); JS frontend that calls the Java backend; database optional.
- ≥2 distinct, meaningful syllabus techniques, **≥1 from Units 4–6**; **CO5** shown through a syllabus-covered randomized algorithm or probabilistic structure (e.g. Bloom filter) — otherwise prior faculty approval of an alternative outcome mapping.
- Original Java implementations of the assessed algorithms (frameworks provide infrastructure only); Java runs assessed algorithms **on the server**; frontend must not substitute browser libraries or precomputed results.
- Synthetic/realistic data with documented inputs, outputs, constraints, assumptions, failure cases; data validation (empty, invalid, duplicates, infeasible, edge cases) without silently wrong results.
- ≥5 documented correctness tests per core algorithm incl. one boundary test; benchmarks at ≥3 increasing sizes; ≥1 baseline comparison with conditions under which it helps; reproducible from the repo with written setup and sample data.
- Syllabus table (eligible techniques): **U1** Counting/Radix/Bucket sort, Quickselect, Median-of-Medians, amortized analysis · **U2** binary/Fibonacci heaps, DSU · **U3** AVL/Red-Black/Splay, segment trees, RMQ · **U4** KMP, tries, radix trees, suffix trees/arrays · **U5** Ford–Fulkerson, Edmonds–Karp, max-flow min-cut, bipartite matching, vertex/set cover, metric TSP approximation · **U6** randomized Quick Sort, randomized selection, Bloom filters, NP/NP-hard/NP-complete analysis.
- Selection rules: generic chatbot / CRUD-only / standalone visualisation does not qualify; approximation algorithms must not claim optimality; **Bloom-filter hits are "possibly present" and need an authoritative check** when exact membership matters.
- Deliverables A–E; viva (~2 min/student: contribution, a key algorithm, correctness, complexity; examiner may alter inputs); demo ~8 min; local app acceptable.
- Rubric (100): problem formulation 10; algorithm selection + original implementation 20; correctness + tests 20; complexity + empirical performance 15; Java backend/API quality 10; frontend integration/usability 5; documentation/reproducibility 5; demo + individual viva 15 (individual).
- Timeline: proposal W11, design review W12, prototype W13, validation/benchmarking W14, final W15 ("faculty will confirm actual dates").
- CO focus shown in the **illustrative** project table: U3→CO2 (inventory), U4→CO3, U5→CO4, U6→CO5.

**MISSING — request from owner/faculty (do not assume):** (1) official CO1–CO5 statements and the authoritative unit→CO mapping (the guide only illustrates); (2) the full course syllabus beyond the technique table; (3) actual week dates / submission dates; (4) **policy on AI-assisted code** and what "your own implementations" permits (D-20); (5) team composition/size and faculty scope approval for a small slice of a larger product; (6) whether a Java test/benchmark framework choice (JUnit/JMH) is expected or free; (7) report page limits are 5–8 — confirm format. **Full compliance cannot be claimed until (1), (4), (5) are answered.**

## 2. Scope framing [TECHNICAL RECOMMENDATION]
Submit a **self-contained, demonstrable slice**: *"Evidence Integrity & Commitment Engine"* of THE FINE PRINT — algorithm-driven, runnable without any LLM (synthetic "model claims" fixtures stand in), so it cannot be mistaken for a chatbot wrapper. It uses `algo-engine` in the same repo as the platform. The wider platform is context, not the graded artefact. **Do not let the LLM-dependent features be on the graded demo's critical path.**

## 3. Candidate placements evaluated (10-point analysis for each recommended algorithm in §4)
| Area (from brief) | Candidate | Verdict |
|---|---|---|
| Quote/citation verification, source anchoring | KMP, suffix array + LCP (U4) | **Recommended (primary)** — foundational to provenance |
| Term lookup / autocomplete of defined terms | Trie / radix tree (U4) | Optional small add-on |
| Duplicate / known-clause screening | Bloom filter (U6/CO5) | **Recommended (CO5)** with exact confirmation |
| Commitment prioritisation | Binary heap (U2) | Recommended (secondary) — honest about scale |
| Cross-document relationship discovery | DSU (U2) | Recommended (secondary) |
| Evidence↔requirement coverage | Bipartite matching / max-flow (U5) | Optional; weakest justification |
| Fee/charge outlier percentile vs reference corpus | Randomized Quickselect (U1/U6) | Alternative CO5 candidate; needs a reference dataset that does not exist yet [OPEN] |
| Event ordering | Sorting (stable) | Not a syllabus-distinct technique here; baseline only |
| Probabilistic document/clause classification | ML classifier | Not syllabus; excluded from CIA scope |
| MinHash/LSH near-duplicate | not listed in the syllabus table | Excluded unless faculty approves (guide requires syllabus-covered CO5 technique) |
| Range queries over timelines | Segment tree (U3) | Rejected: per-user data too small; would be decorative |
| Metric-TSP, set cover, NP analysis | — | No genuine FINE PRINT problem found |

## 4. Algorithm specifications
### A1 — Quote Verifier: KMP and Suffix Array (+LCP) [Unit 4]
1. **Problem:** every "document says" statement must carry a quote that actually appears in the source; Java must verify it (LLMs hallucinate quotes) and compute canonical offsets for anchors. Also finds repeated text across documents (LCP) for boilerplate/duplicate detection.
2. **Why appropriate:** exact substring search with guaranteed linear time (KMP) for one-off; suffix array amortises preprocessing when one document is queried by many quotes (typical: dozens–hundreds per document).
3. **Inputs/outputs:** canonical text T (n chars, NFC-normalised string with offset map), pattern P (m chars after the same normalisation) → list of match start offsets (all occurrences), or empty; SA/LCP arrays.
4. **Correctness:** KMP prefix-function invariants; match positions equal those of reference `indexOf`-loop for all inputs; SA sorted-suffix property and Kasai LCP invariants; proof sketch in the report.
5. **Complexity:** KMP O(n+m) time, O(m) space; SA build O(n log n) (radix prefix doubling) or O(n log² n) (comparison doubling — acceptable for n ≤ 1 MB), query O(m log n + occ), space O(n); LCP Kasai O(n).
6. **Failure modes:** normalisation mismatch (curly quotes, ligatures, hyphenation at line breaks, OCR noise) → false "not found"; Unicode surrogate/grapheme issues; huge n memory; match in wrong location when text repeats.
7. **Limitations:** exact match only — paraphrase is not verification; a verified quote proves presence, **not** that the interpretation is right; fuzzy matching (edit distance) is a separate, labelled feature.
8. **Baselines:** naive nested-loop O(nm); `String.indexOf` (JDK, intrinsic-optimised) as production cross-check; regex. **Expect JDK `indexOf` to be faster on typical text; the benchmark must report this honestly and show where KMP/SA win (adversarial repeats like `aaaa…ab`, many queries per text).**
9. **Benchmark plan:** n ∈ {10 KB, 100 KB, 1 MB, 10 MB} synthetic + real-structured contract-like text; m ∈ {8, 32, 128, 512}; q ∈ {1, 100, 1000} queries per text; adversarial and Devanagari/English mixes; JMH, fixed JVM flags, 5 warm-ups, ≥10 measurements, report mean ± CI; crossover q* where SA beats repeated KMP.
10. **Demonstration:** UI "Verify a quote" panel: paste text + quote, server returns offsets + highlights + algorithm used + comparisons count; live edit of input in viva; own implementation in `algo-engine` with no JDK search calls inside the assessed classes.
**Production stance:** on the critical path (verification gate). Safe because (a) result cross-checked with `indexOf` in production mode with disagreement alert; (b) exact-only semantics; (c) failures only downgrade statements, never upgrade.

### A2 — Approximate membership: Bloom filter [CO5 / Unit 6]
1. **Problem:** fast "possibly seen before" prescreen at ingest: (i) chunk/document fingerprints already in the user's vault; (ii) clause fingerprints matching a reference library of known standard clauses (e.g. standard definitions) so the system can mark "likely standard wording" candidates for exact comparison. Placement (ii) is where memory efficiency could matter as the library grows across insurers/products/versions; (i) is a weak case because SHA-256 unique-index lookup is already cheap — **say so in the report**.
2. **Why appropriate:** no false negatives, tunable false-positive rate, compact bit array; fits "screen then confirm".
3. **Inputs/outputs:** key bytes (normalised-clause hash, 64-bit from own hash function) → `mightContain` boolean; params (n expected, p target) → m, k.
4. **Correctness:** **no false negatives** (property test); measured FPR ≈ (1−e^{−kn/m})^k within a stated tolerance band using fixed seeds; parameter formulas m = −n ln p/(ln2)², k = (m/n) ln 2.
5. **Complexity:** insert/query O(k), space m bits; build O(n k).
6. **Failure modes:** FPR rises beyond design n (saturation); poor hash independence; hash seed reuse; serialisation bugs across restarts.
7. **Limitations:** no deletion (no counting variant here); a "hit" = *possibly present*, **always followed by an exact check**; never user-visible as a fact; never used for security or legal decisions.
8. **Baselines:** `HashSet<Long>`/hash map exact; sorted array + binary search.
9. **Benchmark plan:** n ∈ {10⁴, 10⁵, 10⁶}; bits/element ∈ {6, 10, 14}; measure FPR (10⁶ negative probes), build/query throughput, memory (bytes via JOL/heap measurement) vs HashSet; report crossover where memory saving matters; seeds recorded.
10. **Demonstration:** UI shows filter parameters, insert/probe, live FPR estimate vs theory, and the exact-confirmation step (each "possible hit" resolved to true/false positive).
**Production stance:** prescreen only, behind `ApproximateMembership` interface; if disabled the system remains correct (exact path). Academic module is *not* trust-critical.

### A3 — Commitment scheduler: binary min-heap with index map [Unit 2]
1. **Problem:** global reminder scheduler needs the next-due items across many users and cheap reschedule/cancel when users edit/confirm commitments.
2. **Why appropriate:** O(log n) insert/pop/decrease-key. **Honest limit:** per user n is tens; the heap matters only for the global scheduler (n up to 10⁵–10⁶ entries) — otherwise a DB `ORDER BY` suffices. Fibonacci heap (listed) is not recommended in practice (large constants); may be implemented only for the report's comparison if time allows.
3. **I/O:** (commitmentId, fireAt) → pop due ≤ now; update(commitmentId, newFireAt); cancel.
4. **Correctness:** heap invariant; stable tie-break by id; idempotent pop after restart (DB authoritative).
5. **Complexity:** push/pop/update O(log n), peek O(1), space O(n).
6. **Failure modes:** clock skew/time zones (store UTC; compute due dates with calendar rules in a separate, tested module), lost heap on crash (rebuild from DB), duplicate firing.
7. **Limitations:** ordering only; does not decide *which* commitments are consequential.
8. **Baselines:** sorted list with re-sort; `java.util.PriorityQueue` (support/benchmark only; not the assessed class); DB query.
9. **Benchmark:** n ∈ {10³, 10⁴, 10⁵, 10⁶}; mixed push/pop/update workloads.
10. **Demonstration:** UI timeline of upcoming obligations driven by server heap with live reschedule.
**Production stance:** an in-memory index over authoritative DB rows; failure falls back to DB scan.

### A4 — Document family clustering: DSU [Unit 2]
1. **Problem:** link a policy, endorsements, renewal notices, KFS/sanction letters, correspondence into one agreement using shared identifiers (policy/loan numbers, registration numbers, normalised party names).
2. **Why appropriate:** incremental union of documents as they arrive, near-constant find.
3. **I/O:** documents with extracted identifier set → components + the **reason edges** (identifier → documents) for explanation.
4. **Correctness:** equals connected components of the identifier graph (compared with BFS baseline on random graphs); union by rank + path compression.
5. **Complexity:** O(α(n)) amortised per op.
6. **Failure modes:** **false merges** via common/shared identifiers (e.g. phone numbers, insurer name) → identifier-type allow-list and weights; no split operation → user "unlink" triggers rebuild of affected component from retained reason edges.
7. **Limitations:** transitive closure can over-merge; proposals are shown to the user, never silently applied for legal/consequential use.
8. **Baselines:** BFS/DFS components; pairwise comparison O(n²).
9. **Benchmark:** n ∈ {10³, 10⁴, 10⁵, 10⁶} nodes with varied edge density.
10. **Demonstration:** UI showing clusters with explanation of why documents were grouped; user unlink demo.
**Production stance:** suggestion engine with user override.

### A5 (optional) — Evidence coverage by bipartite matching [Unit 5]
Match required-evidence slots (from a case checklist) to eligible user documents with capacity to expose gaps. **Weak justification:** greedy often suffices; include only to cover Unit 5 or if the team wants more depth; benchmark vs greedy and max-flow (Edmonds–Karp). Not on any critical path.

### Recommended assessed set
**Primary (satisfies rule "≥2 techniques, ≥1 from U4–6" and CO5):** A1 (Unit 4) + A2 (CO5/Unit 6). **Secondary (strengthens problem and rubric):** A3 + A4 (Unit 2). A5 only if time and approval. Team size 2–3 means each student can own one algorithm for the viva (**ownership table to be set by the team**).

## 5. CIA Requirements Traceability Matrix
| Requirement | Syllabus source | Proposed system component | Algorithm/technique | Why it belongs there | Original implementation required | Correctness tests | Benchmark plan | Evidence of completion |
|---|---|---|---|---|---|---|---|---|
| ≥2 distinct techniques, ≥1 from U4–6 | Guide §2, §4 | `algo-engine` | A1 (U4) + A2 (U6) (+A3, A4 U2) | Provenance verification + ingest prescreen are real platform needs | Yes, in Java, no JDK search/Bloom/heap/DSU inside assessed classes | §6 per algorithm | §6 | Code + report §algorithms |
| CO5 randomized/probabilistic | Guide §2; U6 | `algo-engine.bloom` + ingest prescreen | Bloom filter (own hash, double hashing) | Screens before exact checks; scale-sensitive memory | Yes | No-FN property; FPR band; saturation; invalid params; seed repro | FPR/memory/time vs HashSet at 3+ sizes | Test report; FPR table; demo with exact confirm |
| Java for all core algorithms and business logic | Guide §2 | `fineprint-api` modules; worker has no business logic | — | Python only OCR/layout | n/a | Architecture tests (ArchUnit) show business modules are Java | — | Repo layout; ArchUnit report |
| Server-side execution (not frontend) | Guide §6 | `/algo/*` endpoints | all | — | Yes | API contract tests | — | Network trace in demo |
| Frontend integrates with backend | Guide §2, rubric 5 | `web` | — | Input→run→result flow | n/a | e2e (Playwright) | — | Screens, e2e log |
| ≥5 correctness tests/algorithm incl. boundary | Guide §5 | `algo-engine` tests | all | — | — | ≥5 each + property tests vs reference | — | JUnit report |
| Benchmarks ≥3 sizes + baseline | Guide §5 | `algo-bench` (JMH) | all | — | — | — | per §4 | Raw JSON + charts + script |
| Realistic/synthetic data documented | Guide §2 | `data/` corpus | — | — | — | Data validation tests | — | DATA.md |
| Validation/edge cases (empty, invalid, duplicate) | Guide §5 | API validation | all | — | — | Negative tests | — | Test list |
| Reproducibility | Guide §5 | build + Compose + README | — | — | — | CI clean-checkout run | — | README steps verified on clean machine |
| Docs A–E, viva readiness | Guide §8, §10 | `docs/cia/` | — | — | — | — | — | Proposal, report, README, demo script |
| Honest limits (no optimality claims; Bloom = possibly present) | Guide §7 | UI + report | A2, A5 | — | — | UI shows "possibly present → confirmed/false positive" | — | Screenshot |

## 6. Test and benchmark minima (restated for Opus/students)
Per algorithm: ≥5 deterministic unit tests (incl. one boundary) + property tests vs reference implementation; probabilistic tests seeded with tolerance bands; benchmark harness reproducible with one command; results committed as CSV/JSON + generated charts; JVM/hardware recorded.

## 7. Rubric → evidence map
Problem formulation (10): proposal + problem statement tying algorithms to provenance/commitment needs. Algorithm selection + original implementation (20): §4 + code. Correctness/tests (20): test suite + proofs. Complexity/performance (15): JMH results + analysis incl. honest baseline losses. Backend/API (10): module structure, OpenAPI, validation, errors. Frontend (5): algorithm panels. Documentation (5): README + report. Demo/viva (15): each student owns an algorithm; viva Q&A prep sheet in `docs/cia/viva.md`.

## 8. Timeline mapping
Week numbers are the guide's; actual dates UNKNOWN. IMPLEMENTATION_ROADMAP Phase C maps W11–W15 backwards from the faculty-confirmed final date.

## 9. Risks
Academic-integrity ambiguity (D-20); scope creep from the platform into the CIA slice; Bloom filter looking decorative (mitigated by explicit scale argument + exact confirm + honest report); benchmark showing JDK baseline wins (acceptable, report it); team size > 3.
