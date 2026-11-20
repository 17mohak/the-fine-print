# IMPLEMENTATION_ROADMAP.md

v0.2.1 — Architecture **APPROVED v0.2.1 direction** (D-21, 2026-11-20, subject to unresolved decisions, limitations and review gates) · Implementation **AUTHORISED FOR TRACK P, P0 ONLY** (P1–P7 and Track C C0–C3 NOT AUTHORISED) · Opus handover **READY FOR P0 ONLY** · Consumer validation **WAIVED AS A DEVELOPMENT GATE, NOT PASSED**. Each of P1–P7 and C0–C3 needs its own recorded authorisation; P1 additionally needs G0 passed and G0-SEC findings addressed or consciously accepted by the owner. (v0.1 in `_archive/architecture_v0.1/`; **phases renumbered** — see mapping.) 2026-11-20. Durations are not given: team capacity and CIA dates are unknown [OPEN].

**Renumbering (v0.1 → v0.2):** old P1 (algo-engine + CIA slice) → **Track C**; old P2 → **P1**; P3 → **P2**; P4 → **P3**; P5 → **P4**; P6 → **P5**; P7 → **P6**; P8 → **P7**. Any reference to "Phase 8/Phase 2…" in older documents is stale; this file is authoritative.

**Two independent tracks.** **Track P (platform)** P0–P7 never depends on student algorithm delivery. **Track C (CIA)** C0–C3 is independent of platform phases except for shared P0 scaffolding and `core-api` SPIs. Principles: vertical slices; synthetic data only until production gates; benchmarks/metrics are committed artefacts; deviations → ADR → owner review.

## Track P — Platform
### P0 — Repository, CI, SPIs, ground rules
Deliver: monorepo (handover §8), Gradle multi-module, `core-api` (SPIs: `QuoteVerifier`, `ApproximateMembership`, `ReminderIndex`, `DocumentLinker`, DTOs), Docker Compose (Postgres, MinIO, IdP dev, worker stub), CI (build, tests, ArchUnit, dependency + secret scan, SBOM), ADR template, OpenAPI skeleton, commit-attribution rules.
Accept: clean-checkout setup + tests pass on a fresh machine; ArchUnit enforces (a) `platform/*` must not depend on `cia-*`, (b) domain modules don't depend on each other, (c) only `modelgateway` calls providers; Gradle dependency check proves `cia-*` absent from the platform runtime classpath.
**P0 limits (AUTHORISED under D-21 with exactly these limits):** infrastructure only — it must NOT implement encryption/key management, RLS/tenant context, the worker or broker, canonicalisation/provenance logic, search indexes, production `QuoteVerifier` adapters, or any assessed algorithm; the `cia/` directory may exist only as empty placeholders with a README stating Track C is not authorised. No real documents or sensitive data in any environment.
Gate G0: owner confirms versions (#2) and the D-20 path (search architecture is decided: D-25).
**Gate G0-SEC (precondition for P1):** a security-specialist review of the encryption/key design (SECURITY sections 3-4), RLS/tenant-context design (section 6) and worker-isolation design (ARCHITECTURE section 4) must be completed and its findings recorded **before P1 implements those controls**. This review is not a production-security approval.

### P1 — Evidence core
Deliver: `identity-access` (OIDC, TenantContext), `crypto` (key hierarchy per SECURITY §3 with a **dev-local KMS stub**; real KMS undecided), `documents` (magic-byte/size validation, hashing, encrypted storage, status machine), **minimal worker + broker** (text-layer extraction and page rendering only; mTLS broker protocol; sandbox basics), `canonical` (L0/L1/L2, edit map, code-point offsets, grapheme alignment, bbox mapping), `provenance` (assertions/anchors, production `QuoteVerifier`, anchor lifecycle), audit chain, RLS + tenant-context framework, **ephemeral agreement-scoped in-memory index per SECURITY section 5 (lazy build, byte-bounded, TTL/LRU eviction, tenant-partitioned, heap-only; no persisted plaintext index; cross-vault search deferred)**; `canonical` preserving **L0 exactly**, derived S0 and pinned-segmenter (Unicode >= 15.1) grapheme clusters, upload/view UI with five-kind components on fixtures.
Accept: SYSTEM_DESIGN §2.7 test catalogue green (D1–D6, L1–L2, H1–H4, O1–O3, R1–R3, W1, I1–I3, **G1–G10**, P1–P2) including L0 immutability and the pinned-segmenter/GB9c check; index tests: two-user partition canaries, byte bound and TTL/LRU eviction, rebuild-after-eviction, no plaintext index on disk (filesystem scan), memory-budget exhaustion fallback; two-user isolation suite (IDOR, search, object streaming, canary strings, pooled-connection check, RLS schema lint); DOCUMENT_FACT only with `EXACT_UNIQUE`/declared-multi anchors; deletion removes ciphertext and destroys keys (test) and restore replays the deletion ledger (test); worker cannot reach any network except broker (network test); untrusted file parsing absent from Java core (ArchUnit).
Gate G1 (requires G0-SEC findings addressed or consciously accepted by the owner).

### P2 — OCR, worker hardening and intelligence pipeline
Deliver: OCR port + Tesseract baseline (cloud OCR only if owner+legal approve), layout/tables, worker contract tests, lease/heartbeat/dead-letter, classification, segmentation, cross-references, **model gateway**, schema-constrained extraction + Java verification, injection defences + red-team CI, golden corpus (synthetic first) + evaluation harness.
Accept: pipeline reaches READY on corpus; metrics recorded against owner-set thresholds (set at G2); injection suite: zero followed instructions; sandbox/network restrictions verified; failure and quarantine states reachable and tested; OCR-noise cases O1–O3 behave as specified.
Gate G2 (owner sets thresholds; reviews cost/latency).

### P3 — Knowledge layer and domain modules (architecture proof)
Deliver: instruments/versions/provisions/rules/sources with **verification ladder V0–V4**, reviewer workflow, deterministic evaluator (effective dating) gated on V3+, `generic-agreement` + two reference modules chosen to stress different engine properties (structured-document lending; unstructured-policy insurance) + one thin module (tenancy or subscriptions) — architecture proof, **not market selection**; Q&A with abstention; comparison.
Accept: no rule below V3 drives deterministic output (test); mirror/secondary badges rendered; effective-date boundary tests (e.g. before/after 1 Jan 2026 bank prepayment rule); bank vs NBFC scope not generalised; INTERPRETIVE rules never yield verdicts; UNKNOWN records generated; mirror→official upgrade workflow exercised with a fixture.
Gate G3.

### P4 — Protect
Deliver: commitment candidates → confirm flow; DB-backed `ReminderIndex` adapter; reminders (in-app/email only if approved); version-change → NEEDS_REVIEW; timeline UI; `RULE_DERIVED` candidates only from V3+ rules.
Accept: no reminder from unconfirmed candidates (test); restart-safe idempotent firing; timezone/DST/calendar tests; cross-tenant scheduler isolation test (system role sees no payloads).

### P5 — Act
Deliver: cases, events, evidence (encrypted), ordered timeline, drafts with per-sentence provenance, checklists, channel catalogue (GUIDED/ASSISTED), export pack; `external_submission` absent or flag-disabled with tests proving it cannot run.
Accept: safety suite (no unsupported legal conclusions); packs traceable; capability gating tests; no portal automation code exists.

### P6 — Domain breadth completion
Deliver: remaining modules (tenancy with state gating, subscriptions, warranties, motor/life variants), localisation scaffolding (Hindi first — assumption), mobile capture polish.
Accept: per-module corpus/rule-pack/safety tests; no cross-module imports.

### P7 — Hardening (required before any real-user use)
Deliver: independent security review, pen-test fixes, perf/load, dashboards/alerts, backup/restore drill incl. ledger replay, incident runbook, retention verification, legal outputs integrated, threat-model refresh.
Accept: all SECURITY §10 gates closed with evidence; owner sign-off. **Real users or real sensitive documents remain prohibited until P7 closes.**

## Track C — CIA slice (independent; **assessed code is student-authored until faculty confirms the AI policy**)
### C0 — Faculty inputs and scope approval
Obtain: CO definitions/unit mapping, actual dates, AI-assistance policy, team size, faculty approval of the slice. Output: signed-off proposal (Deliverable A). Blocks C1 authorship model.
### C1 — Student implementation of assessed algorithms (`cia-algorithms`)
Students implement A1 (KMP; suffix array + LCP), A2 (Bloom filter incl. hash), A3 (heap), A4 (DSU) behind the `core-api` SPIs; students author tests, baselines and benchmarks. Opus may provide only what the CIA matrix §0 allows.
### C2 — `cia-demo` integration and evidence
Runnable app (no DB server, network, LLM or worker): UI panels for quote verification, Bloom explorer with exact-confirmation, scheduler timeline, document families; synthetic data + DATA.md; JMH results committed; README reproducible from clean checkout.
Accept: ≥5 tests per algorithm incl. boundary + property tests vs reference; ≥3 benchmark sizes + baseline per algorithm; honest results (including baseline wins); Bloom no-false-negative property and FPR within declared band; demo works offline.
### C3 — Report, demo, viva
Technical report (5–8 pp), demo script, viva sheet; each student owns an algorithm.
Gate GC: owner (and faculty as applicable) reviews against CIA matrix and missing-information list.
Week mapping: faculty guide's W11 proposal, W12 design review, W13 prototype, W14 benchmarking, W15 final — **actual dates unknown**.

## Dependencies
P0 → P1 → P2 → P3 → {P4, P5} → P6 → P7. Track C needs P0 only (SPIs). No platform phase requires Track C output. Opportunity-validation research (RESEARCH_BACKLOG §D) may run any time and can reprioritise P3/P6 module order.

## Review gates
G0–G7 for Track P; GC for Track C. Each: demo + evidence pack + deviations list + decision record; material architecture changes need owner approval in DECISION_LOG.
