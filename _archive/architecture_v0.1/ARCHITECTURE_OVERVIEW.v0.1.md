# ARCHITECTURE_OVERVIEW.md

Status: **PROPOSED — not reviewed, not approved.** Version 0.1 · 2026-11-20. Labels per PROJECT_FOUNDATION. Detail in SYSTEM_DESIGN.md; technology reasoning in TECHNOLOGY_DECISIONS.md.

## 1. Architectural thesis [TECHNICAL RECOMMENDATION]
1. **A modular monolith in Java** is the platform core. Complexity is added only where a requirement forces it. The one forced split: a **Python document worker** (OCR/layout/model inference) because the best OCR/layout tooling is Python/native and must be sandboxed from the core.
2. **Provenance is the spine.** Everything user-visible is an *Assertion* of one of six kinds, tied to evidence anchors in the original document or to a versioned regulatory provision. Java verifies anchors; an LLM can never create a "document says" statement on its own.
3. **Shared foundation, pluggable domain modules.** Foundation = ingestion, canonical text, structure, provenance, knowledge store, commitments, cases, security. Domain modules (insurance, lending, tenancy, subscriptions, warranties, generic-agreement) implement a Module SPI and own their taxonomies, extraction schemas, rule packs and workflows. No module may be required for the others to run.
4. **Language models are untrusted components** behind a Java model gateway: schema-constrained outputs, validators, no tools in extraction, citation re-verification, abstention.
5. **Consequential actions are gated**: reminders only from confirmed commitments; external submission is a separate, default-off capability.

## 2. Context and containers
```
 Browser (React SPA)  ──HTTPS/JSON──▶  fineprint-api (Spring Boot, Java)  ◀── OIDC IdP
                                         │ modules (in-process, enforced by ArchUnit):
                                         │  identity-access · documents · provenance · knowledge · domains/*
                                         │  understand · commitments · cases · integrations · audit · algo-engine
                                         │
            ┌────────────────────────────┼─────────────────────────────┐
            ▼                            ▼                             ▼
     PostgreSQL 16              Object store (S3-compatible)     Job queue (Postgres SKIP LOCKED)
 (data, FTS, audit, queue,      originals + derived artefacts           │
  optional pgvector)            envelope-encrypted per user             ▼
                                                              doc-worker (Python, sandboxed)
                                                              OCR · layout/tables · image prep
                                                                      │
 fineprint-api ──▶ Model Gateway (Java) ──▶ LLM provider(s) / self-hosted model   [data-exposure controls]
 fineprint-api ──▶ Integrations (Java)  ──▶ official channels   [GUIDED/ASSISTED now; CONNECTED disabled]
```
Trust boundaries: (T1) internet→API; (T2) API→doc-worker (hostile files); (T3) API→model provider (data leaves our control); (T4) API→official portals; (T5) staff/ops→data; (T6) object store/backup. See SECURITY_AND_PRIVACY.md.

## 3. Major components and responsibilities
| Component | Language | Responsibility | Must not |
|---|---|---|---|
| `identity-access` | Java | AuthN (OIDC), authZ (owner-scoped + role), session, consent records | store passwords (delegated to IdP) |
| `documents` | Java | Upload validation, hashing, dedup, versioning, status machine, storage, reprocessing | alter originals |
| `doc-worker` | Python | OCR, layout/tables, page images, returns canonical-text candidate + confidences | access DB/object store directly beyond signed job URLs; call the internet |
| `canonical` | Java | Normalised text model with offset map to pages/bboxes; structure tree | trust worker output without validation |
| `provenance` | Java | Assertion + EvidenceAnchor model, verification, audit, corrections | emit DOC_FACT without verified anchor |
| `algo-engine` | Java (pure, no framework) | Original algorithms: KMP/trie/suffix array, Bloom filter, heap scheduler, DSU, etc. | depend on Spring |
| `knowledge` | Java | Instruments, versions, provisions, rules, effective dating, verification status, review workflow | serve unreviewed rules as verified |
| `domains/*` | Java (+ data packs) | Per-domain taxonomy, extraction schemas, checks, workflows via SPI | leak into foundation |
| `understand` | Java | Classification, segmentation, extraction orchestration, Q&A, comparison, missing-info detection | answer without verified citations |
| `modelgateway` | Java | Provider abstraction, prompt/schema registry, redaction, budgets, logging policy, injection defences | send data to non-approved providers |
| `commitments` | Java | Candidate→confirmed lifecycle, schedule, reminders | auto-create consequential reminders |
| `cases` | Java | Case, events, evidence, communications, drafts, submission-prep, outcomes | submit externally |
| `integrations` | Java | Channel registry, capability records, guided links, (future) connectors | bypass auth/CAPTCHA |
| `audit` | Java | Append-only audit, hash-chained | log document content |
| `web` | TS/React | UI per PRODUCT_EXPERIENCE_SPEC | run assessed algorithms |

## 4. Key data flows
**Ingest→Understand:** upload → validate (type/size/magic bytes, AV scan) → store original (immutable) → job → worker OCR/layout → canonical text (Java validates, builds offset map) → classify → segment clauses → extract entities/obligations (LLM structured output) → **Java verifiers** (quote exists via KMP/suffix array; amounts/dates parse; arithmetic) → assertions with anchors → knowledge join (applicable rules by effective date) → results with provenance kinds.
**Ask:** question → retrieval over verified spans (FTS first; embeddings only if benchmarks justify) → LLM answer constrained to span IDs → citation verifier → abstain if unsupported/conflicting.
**Protect:** extractor proposes CANDIDATE commitments → user confirms/edits → heap scheduler → reminders.
**Act:** user opens case → attaches documents/events → timeline built (ordered events) → clauses/rules linked → draft + checklist → user exports/guided link; submission capability off.

## 5. What is deliberately NOT in v1 architecture
Microservices, Kubernetes, Kafka, graph DB, separate vector DB, workflow engines, rules engines (Drools), mobile native apps, email ingestion (needs authorisation/privacy review), connected portal integrations. Each has a stated trigger for revisiting (TECHNOLOGY_DECISIONS §Revisit triggers).

## 6. Quality attributes
Testability (algo-engine pure; golden-document corpus), Reproducibility (pinned versions, seeded randomness, Docker Compose), Observability (OpenTelemetry, redacted structured logs, extraction-quality metrics), Security (default-deny authZ, per-user keys), Maintainability (ArchUnit-enforced module boundaries, Flyway migrations).

## 7. Status record
Architecture PROPOSED. Not reviewed. Not approved. Build NOT authorised.
