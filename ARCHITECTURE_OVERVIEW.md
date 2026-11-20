# ARCHITECTURE_OVERVIEW.md

Status (D-21, recorded 2026-11-20): Architecture **APPROVED v0.2.1 direction** (D-21, 2026-11-20, subject to unresolved decisions, limitations and review gates) · Implementation **AUTHORISED FOR TRACK P, P0 ONLY** (P1–P7 and Track C C0–C3 NOT AUTHORISED) · Opus handover **READY FOR P0 ONLY** · Consumer validation **WAIVED AS A DEVELOPMENT GATE, NOT PASSED**. (v0.1 preserved in `_archive/architecture_v0.1/`.) 2026-11-20.
Labels: VERIFIED FACT · RESEARCH FINDING · TECHNICAL RECOMMENDATION · ASSUMPTION · OPEN QUESTION · DEFERRED DECISION.

## 0. Change summary v0.1 → v0.2
| # | Owner requirement | Change | Where |
|---|---|---|---|
| 1 | CIA vs production separation | Assessed algorithms moved to isolated `cia-algorithms`; production uses SPI interfaces with standard-library adapters; promotion criteria, shadow mode, fallbacks defined; `cia-demo` is independently runnable | §2, SYSTEM_DESIGN §4, CIA matrix §0/§2/§4 |
| 2 | Worker boundary | "No egress except signed URLs" contradiction removed: worker has **no storage URLs and no internet**; it connects only to a Java **worker broker** over mTLS and streams bytes through it; all parsing of untrusted bytes is in the worker; model calls only via Java gateway | §4, SYSTEM_DESIGN §3 |
| 3 | Encryption/security | Key hierarchy for originals, derived text, assertions, case evidence; envelope encryption, rotation, crypto-shredding, tenant context, RLS enforcement, job isolation; search consequence of encrypted text stated | SECURITY_AND_PRIVACY §3–§6, SYSTEM_DESIGN §2 |
| 4 | Canonicalisation/provenance | Code-point offsets, three text layers, reversible edit-map, grapheme alignment, bbox mapping, anchor invalidation, test catalogue | SYSTEM_DESIGN §2, §5 |
| 5 | Regulatory knowledge | Verification ladder V0–V4; deterministic evaluation allowed only from reviewed official-host rules; mirror = informational | SYSTEM_DESIGN §7 |
| 6 | CIA authorship | Explicit student/Opus boundary; no Opus-written assessed bodies until faculty confirms; missing items visible | CIA matrix §0, §6; handover §0, §17 |
| 7 | Consistency | Roadmap re-cut into Platform phases P0–P7 + CIA Track C; stale references fixed across all documents; handover has a decision-rights table | ROADMAP; handover §24 |
| 8 | Technology decisions | Ownership/readiness columns (now / owner / faculty / legal / security) | TECHNOLOGY_DECISIONS |
| 9 | Review | Second review performed; unresolved issues listed | DECISION_LOG §Review v0.2 |
**Material changes vs v0.1 to be aware of:** (a) user-document text is **no longer indexed in Postgres FTS** (encryption conflict) — resolved by D-25 (ephemeral agreement-scoped index); (b) PDFBox no longer parses untrusted files inside the Java core; (c) `algo-engine` module renamed/split; (d) roadmap renumbered.

## 0a. v0.2.1 changes (final revision)
D-25 search adopted: ephemeral agreement-scoped in-memory index, no persisted plaintext index, cross-vault search deferred (SECURITY section 5). L0 stays the exact worker token stream; grapheme segmentation runs over derived S0 with mappings to all tokens/boxes; full UAX #29 coverage incl. Indic conjuncts (SYSTEM_DESIGN 2.1). Production verification is exact matching on L1 with grapheme enforcement; L2 is candidate retrieval only (2.3, 5.3). The v0.2 broker "token merge" rule is withdrawn.

## 1. Architectural thesis (unchanged in spirit) [TECHNICAL RECOMMENDATION]
1. Modular monolith in Java as the platform core; one forced split: a sandboxed Python worker.
2. Provenance is the spine: every user-visible statement is an Assertion of one of six kinds with verified anchors or versioned regulatory citations.
3. Shared foundation + independent domain modules; no module required for others.
4. Language models are untrusted components behind a Java model gateway.
5. Consequential actions gated (confirmed commitments only; submission default-off).
6. **(new)** Academic algorithm work is isolated from production; production relies on boring, well-tested components until an algorithm earns promotion with evidence.

## 2. CIA vs production (requirement 1)
**Principle:** the CIA requires original Java implementations of specific syllabus algorithms; the product requires reliability. These goals conflict if the assessed code becomes a production dependency. v0.2 therefore has **two implementations behind shared interfaces**.
```
core-api (interfaces + DTOs: Anchor, Assertion, Commitment... ; SPIs below)
   ├─ platform/* production adapters (JDK/standard libs; no import of cia-algorithms)
   └─ cia-algorithms (assessed, student-authored; depends only on core-api SPI types) ──▶ cia-demo (separate runnable app)
```
**SPIs (production default adapter · assessed implementation · product relevance · promotion trigger):**
| SPI | Production default | Assessed (isolated) | Genuine product relevance | Promotion criteria / status |
|---|---|---|---|---|
| `QuoteVerifier` (find all exact occurrences of a quote in **L1** canonical text) | Exact code-point matching on L1 with the same deterministic normalisation as L1 (JDK string search) plus L1 grapheme-boundary enforcement; L2 never verifies | KMP; suffix array + LCP | Verification gate for DOCUMENT_FACT. Production need is satisfied by the baseline at expected sizes (≤ ~1 MB text, ≤ ~10³ quotes) | **Not planned for promotion.** Eligible only as a *shadow cross-checker* (see below) or if cross-document repeated-text detection (LCP) becomes a product feature |
| `ApproximateMembership` | Exact set (hash set in memory / DB lookup) | Bloom filter | Only valuable if a reference-fingerprint library outgrows the memory budget | **Not needed today** (no such library exists); promotion requires measured memory pressure |
| `ReminderIndex` | DB `next_fire_at` index + `SKIP LOCKED` polling | Indexed binary min-heap | Global scheduling at very large scale | **Not needed**; DB is authoritative anyway |
| `DocumentLinker` | Simple in-memory connected-components over identifier edges (per user) | DSU with reason edges | Linking documents into agreements — per-user graphs are tiny | **Not needed**; assessed DSU stays demonstration |
**Promotion criteria (all required; ADR + owner approval):** (1) documented product need with a *measured* failure of the production baseline against an SLO/memory budget; (2) pre-declared benchmark threshold met on realistic data (e.g., ≥2× or fits budget); (3) differential and property tests against the baseline over a large randomised corpus plus fuzzing; (4) failure-mode analysis and a defined fallback; (5) shadow-mode run (both execute; disagreement logged/alerted; production uses baseline) for an agreed period with zero unexplained disagreements; (6) independent code review by someone other than the author; (7) authorship/provenance recorded (CIA authorship policy). **Default is no promotion.**
**Fallback behaviour:** production never imports `cia-algorithms`; build rule + ArchUnit enforce. If any assessed code is ever loaded in shadow mode and fails or times out, the call is dropped and production result stands.
**Academic isolation:** separate Gradle modules (`cia-algorithms`, `cia-bench`, `cia-demo`), own packages, own tests/benchmarks, no Spring/DB/network in `cia-algorithms`; platform modules must not depend on them (ArchUnit + Gradle dependency check in CI). Assessed classes must not call JDK search/heap/union-find/Bloom equivalents.
**Integrated yet independent:** `cia-demo` is a runnable application composed of `core-api` + CIA adapters + a small evidence-integrity UI and synthetic sample data (the "Evidence Integrity & Commitment Engine" slice). It reuses the platform's data vocabulary (Anchor, Assertion kinds, Commitment candidate vs confirmed) so it is visibly part of THE FINE PRINT concept, but runs with no database server, no network, no LLM, no worker.
**Consequence:** the platform schedule no longer depends on student algorithm delivery, and academic code cannot degrade legal-safety or provenance guarantees.

## 3. Context and containers
```
 Browser (React SPA) ──HTTPS──▶ fineprint-api (Spring Boot, Java; modular monolith)
                                  │ modules: identity-access · documents · canonical · provenance · knowledge · domains/*
                                  │          understand · modelgateway · commitments · cases · integrations · audit · crypto · workerbroker
      ┌───────────────────────────┼─────────────────────────────────────────┐
      ▼                           ▼                                         ▼
 PostgreSQL (RLS, encrypted     Object store (ciphertext only;          Job tables (Postgres)
 payload columns)               DEK-per-object)                                │
                                                       workerbroker (Java, mTLS, internal net) ◀── pull/stream ── doc-worker (Python, sandbox)
 fineprint-api ──▶ ModelGateway (Java) ──▶ LLM provider(s) / local model  [OWNER/LEGAL decision pending]
 fineprint-api ──▶ integrations (Java) ──▶ official channels [GUIDED/ASSISTED only]
 cia-demo (separate runnable app; no network, no DB server)
```
Trust boundaries: T1 internet→API · T2 broker↔worker (hostile bytes) · T3 gateway→model provider · T4 integrations→portals · T5 staff/ops→data · T6 storage/backup · T7 **KMS** (key custody).

## 4. Python worker boundary (requirement 2)
**Responsibilities (only):** render pages to images; decide text-layer vs OCR per page; OCR with word boxes, confidence, language hints; image pre-processing (deskew/denoise/rotate); layout/table detection; sanitise by *re-rendering* (it emits derived text/images/JSON, never passes through original active content). **Not:** classification, extraction, rules, LLM/model calls, DB or object-store access, user/tenant identity, retries policy, business logic.
**Network model (resolves v0.1 contradiction):** the worker has **no internet and no object-storage access and receives no signed storage URLs.** It makes a single outbound **mTLS connection to the internal worker broker** (allow-listed destination; no DNS/other egress; no inbound ports). Bytes flow *through* the broker: broker validates job lease, streams original bytes (decrypted in memory by Java for that job only, or re-encrypted transport) to the worker, and receives results back. The worker authenticates with its mTLS identity plus a **single-use, job-bound, short-lived token**. The broker enforces size caps, content-length limits, timeouts and result-schema validation.
**Isolation:** non-root container; read-only root FS; per-job writable tmpfs wiped after each job; seccomp/AppArmor profile; CPU/memory/pids/time cgroup limits; **each parser/OCR invocation runs in a child process with no network namespace** and resource limits; no shared state between jobs (stateless; restart policy: recycle after N jobs); no credentials other than its mTLS identity; no access to host paths; logging to stdout with no content. *Hardened alternative (DEFERRED DECISION):* ephemeral container per job (`--network=none`) launched by a runner — rejected as default because the app would need container-runtime control (privilege escalation risk).
**Data flow:** (1) API stores encrypted original; creates job row (user-scoped). (2) Broker leases job to worker; Java decrypts original in memory and streams to worker over mTLS. (3) Worker processes in a sandboxed child; emits result stream (pages, tokens with bbox/confidence, tables, warnings, engine versions). (4) Broker validates schema, sizes, coordinate bounds, text sanity; Java builds canonical text and encrypts/stores artefacts. (5) Worker wipes tmpfs.
**Failure handling:** job lease + heartbeat; timeout → kill child → lease expires → retry (max 3, backoff) → `FAILED_EXTRACTING` with error code and dead-letter record (no content); resource-limit kills recorded as `RESOURCE_EXCEEDED`; partial results discarded unless the full page set validates; poison-job quarantine; idempotent by `job_id`; worker crash safe (no state); alert on failure-rate spikes.
**Model providers:** accessed only via the Java `ModelGateway`. If a vision/LLM OCR path is ever wanted, Java sends page images through the gateway; the worker never calls providers. A worker-side provider call would need a separate justified ADR plus owner/legal approval (not proposed).
**Untrusted parsing:** *all* parsing of untrusted bytes (including PDF text-layer extraction) occurs in the worker; the Java core only does magic-byte/size checks and hashing. (v0.1 allowed PDFBox in the core — reversed.)
**Residual risks:** parser zero-days within the sandbox, worker-memory residency of plaintext during a job, broker as a high-value chokepoint (mitigated by validation, mTLS, minimal surface). Not claimed solved.

## 5. Component responsibilities
| Component | Language | Responsibility | Must not |
|---|---|---|---|
| `core-api` | Java | Interfaces/DTOs/SPIs shared by platform and CIA | contain logic |
| `identity-access` | Java | OIDC, authZ, TenantContext creation, consent | store passwords |
| `crypto` | Java | Key hierarchy, envelope encryption, rotation, shredding (SECURITY §3) | log keys |
| `documents` | Java | Upload validation (magic bytes, size), hashing, versioning, status machine | parse untrusted content |
| `workerbroker` | Java | Job leasing, byte streaming, result validation | trust results |
| `doc-worker` | Python | See §4 | anything else |
| `canonical` | Java | Preserves L0 unchanged; derives S0, grapheme segmentation (pinned segmenter), L1, edit map, bbox mapping | mutate L0; accept worker output unvalidated |
| `provenance` | Java | Assertions, anchors, verification via `QuoteVerifier`, audit | emit DOCUMENT_FACT without unique verified anchor |
| `knowledge` | Java | Instruments, versions, provisions, rules, verification ladder | evaluate rules below V3 |
| `domains/*` | Java + data | Domain SPI implementations | depend on each other |
| `understand` | Java | Classification, segmentation, extraction orchestration, Q&A, comparison | answer without verified support |
| `modelgateway` | Java | Provider abstraction, redaction, schemas, budgets | call unapproved providers |
| `commitments` | Java | Candidate→confirmed lifecycle, reminders via `ReminderIndex` | remind from unconfirmed |
| `cases` | Java | Case, events, evidence, drafts, prep | submit externally |
| `integrations` | Java | Channel registry, capability records, guided links | bypass auth/CAPTCHA |
| `audit` | Java | Hash-chained audit | log content |
| `cia-algorithms`/`cia-bench`/`cia-demo` | Java | Assessed algorithms, benchmarks, demo | be imported by platform |
| `web` | TS/React | UI | run assessed algorithms (CIA demo panels call server) |

## 6. Key flows (updated)
Ingest: upload → validate → encrypt+store original → job → broker→worker → canonicalise (Java) → classify → segment → extract (quote-required) → **`QuoteVerifier` (production adapter)** + deterministic parsers → assertions/anchors → knowledge join (rules at V3+ only for deterministic output) → results.
Ask: retrieval over an **ephemeral, agreement-scoped in-memory index built lazily from decrypted L1** (D-25; SECURITY §5; never persisted) → verified spans → gateway → citation re-verification → abstain if unsupported.
Protect: candidates → user confirms → `ReminderIndex` (DB-backed) → reminders.
Act: case → evidence → ordered timeline → clauses/rules → draft → user submits (GUIDED/ASSISTED).

## 7. Deliberately not in v0.2
Microservices, Kubernetes, Kafka, vector/graph DB, workflow/rules engines, native mobile, email ingestion, connected portal integrations, E2E ("zero-knowledge") encryption (server-side processing and LLM calls require plaintext — stated, not hidden).

## 8. Status record
Architecture APPROVED v0.2.1 direction (D-21, 2026-11-20), subject to the documented unresolved decisions, limitations and review gates. Implementation AUTHORISED FOR TRACK P, P0 ONLY; P1–P7 and Track C NOT AUTHORISED. Handover READY FOR P0 ONLY. Validation WAIVED AS A DEVELOPMENT GATE, NOT PASSED. Owner-input items listed in TECHNOLOGY_DECISIONS §B.
