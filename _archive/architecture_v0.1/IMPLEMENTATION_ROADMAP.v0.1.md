# IMPLEMENTATION_ROADMAP.md

Status: PROPOSED · 2026-11-20 · **No implementation has begun; build NOT authorised.** Phases are dependency-ordered; durations are deliberately not given because team capacity and CIA dates are unknown [OPEN]. Each milestone has acceptance criteria that must be demonstrated with evidence (tests, reports), then a **review gate** with the owner before the next phase.

Principles: vertical slices over layers; every phase leaves a runnable system; synthetic data only until production gates; benchmarks and golden-corpus metrics are committed artefacts; no phase silently changes approved architecture (deviations → ADR → owner review).

## Phase 0 — Repository, CI, ground rules
Deliver: monorepo skeleton (see handover §8), Gradle multi-module build, Docker Compose (Postgres, MinIO, worker stub, IdP dev), CI (build, tests, ArchUnit, dependency + secret scan), ADR template, OpenAPI skeleton, commit/author attribution rules.
Accept: clean-checkout `make up && make test` passes on a fresh machine; ArchUnit forbids `algo-engine`→Spring and forbidden module edges; secret-scan and SBOM jobs run.
Gate G0: owner confirms stack versions (Java/Spring pinned) and D-20 policy.

## Phase 1 — `algo-engine` + CIA slice (**earliest, due to academic deadlines [dates UNKNOWN]**)
Deliver: A1 KMP + suffix array/LCP, A2 Bloom filter, A3 binary heap with index map, A4 DSU (+ optional A5), reference implementations/baselines, JUnit + jqwik tests, JMH benchmark harness with scripts, synthetic contract-like corpus generator + sample documents, `/algo/*` REST endpoints, minimal React panels (verify quote, Bloom explorer, scheduler timeline, document families), CIA docs skeleton (proposal, report outline, README, viva sheet).
Accept: ≥5 deterministic tests + property tests per algorithm incl. boundary; **no false negatives** property for Bloom; FPR within pre-declared band at ≥3 sizes; benchmarks at ≥3 sizes × baselines with raw results committed and one-command reproduction; honest results incl. cases where JDK baselines win; assessed classes contain no JDK search/heap/union-find shortcuts (checked by test/ArchUnit); end-to-end demo path works without any LLM or network.
Gate G1: owner + (where applicable) faculty review of CIA slice vs `CIA_REQUIREMENTS_MATRIX.md` and the missing-information list.

## Phase 2 — Evidence core
Deliver: identity (OIDC), `documents` (validation, hashing, versioning, object storage, status machine), canonical text with offset map for **text-layer PDFs** (PDFBox), assertion/anchor/audit model, quote verification wired to A1, document families wired to A4, Postgres RLS + tenant-isolation tests, key-wrapping abstraction (per-user data keys wrapped by a dev-local KMS stub; real KMS decision deferred) with a test that originals are unreadable without the key, upload/view UI with the five-kind components using fixture assertions.
Accept: two-user isolation test suite green (incl. IDOR, search, object URLs); every DOCUMENT_FACT in fixtures has a verified anchor or is rejected; anchors survive reprocessing (rebuilt, never reused silently); audit chain verifiable; deletion removes original + derived (test).
Gate G2.

## Phase 3 — Document worker and intelligence pipeline
Deliver: sandboxed Python worker (OCR via port, layout/tables), worker contract tests, classification, segmentation, cross-reference resolution, **model gateway** (provider abstraction, schema registry, redaction profile, budgets), schema-constrained extraction with Java verifiers, injection defences + red-team suite, golden corpus (synthetic first) + evaluation harness with thresholds.
Accept: pipeline reaches READY on corpus; metrics recorded (field accuracy, citation precision/recall, abstention precision, deadline correctness) against thresholds the owner sets; injection suite: zero followed instructions; sandbox restrictions verified; failure states reachable and tested.
Gate G3: owner reviews metrics and cost/latency per document.

## Phase 4 — Knowledge layer and domain modules (architecture proof across domains)
Deliver: instruments/versions/provisions/rules with host-class + verification status, reviewer workflow, deterministic evaluator (effective dating), `generic-agreement` module, **two reference domain modules chosen to stress different engine properties (structured-document lending/KFS; unstructured-policy insurance)** + one thin module (tenancy or subscriptions) to prove breadth and state-gating; seed rules only from sources in Gate1B/02 & /08 with host class preserved; document Q&A with citation verification and abstention; comparison.
Accept: rule applicability tests at effective-date boundaries (e.g., before/after 1 Jan 2026 prepayment rule; bank vs NBFC scope not generalised); MIRROR/SECONDARY markers rendered; interpretive rules never produce verdicts; abstention triggers tested; UNKNOWN records generated where info missing.
Note: module selection here is an architecture proof, **not** a market selection.
Gate G4.

## Phase 5 — Protect
Deliver: commitment candidates → confirm/edit/dismiss flow; scheduler (A3 in service layer with DB authority); reminders (in-app/email); version change → NEEDS_REVIEW; timeline UI.
Accept: no reminder from unconfirmed candidates (test); restart-safety and idempotent firing; timezone/DST/calendar-month tests; deadline rules computed from approved rules only.

## Phase 6 — Act
Deliver: cases, events, evidence, timeline ordering, communications, drafts with per-sentence provenance, checklists, channel catalogue with GUIDED/ASSISTED, export pack; `external_submission` code path absent or flag-disabled with tests proving it cannot run.
Accept: drafts contain no unsupported legal conclusions (safety suite); every statement in pack traceable; capability gating tests; no portal automation code exists.

## Phase 7 — Domain breadth completion
Deliver: remaining modules (tenancy with state gating, subscriptions, warranties, motor/life variants), localisation scaffolding (Hindi first, assumption), mobile capture polish.
Accept: each module's golden corpus + rule packs + safety tests pass; no module imports another.

## Phase 8 — Hardening (required before any real-user use)
Deliver: independent security review, pen-test fixes, load/perf tests, observability dashboards/alerts, backup/restore drill, incident runbook, retention tests, DPDP/legal outputs integrated (notices/consent), threat-model update.
Accept: all SECURITY_AND_PRIVACY §5 gates closed with evidence; owner sign-off. **Real users or real sensitive documents remain prohibited until this phase closes.**

## Dependency summary
P0 → P1 (independent of platform) ; P0 → P2 → P3 → P4 → {P5, P6} → P7 → P8. P1 feeds P2 (A1, A4). Opportunity-validation research (RESEARCH_BACKLOG §D) can run in parallel at any time and may reprioritise P4/P7 module order.

## Review gates
G0–G8 as above. Each gate: demo + evidence pack + deviations list + decision record. Material architecture changes need owner approval via DECISION_LOG.
